package br.fecap.pi.ubersafestart;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import br.fecap.pi.ubersafestart.model.AudioRecording;
import br.fecap.pi.ubersafestart.utils.AudioRecordingManager;

public class MyRecordingsActivity extends AppCompatActivity {

    private static final String TAG = "MyRecordingsActivity";
    private static final int REQUEST_PERMISSION_CODE = 1001;

    // UI Components
    private RecyclerView recyclerViewRecordings;
    private LinearLayout layoutNoRecordings;
    private LinearLayout layoutPlaybackControl;
    private TextView textViewPlayingFile;
    private SeekBar seekBarPlayback;
    private TextView textViewCurrentTime;
    private TextView textViewTotalTime;
    private ImageButton buttonPlayPause;
    private ImageButton buttonStopPlayback;
    private ImageButton buttonClosePlayback;

    private AudioRecordingManager recordingManager;
    private RecordingsAdapter adapter;
    private List<AudioRecording> recordings;

    private Set<String> sentRecordings = new HashSet<>();

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private Handler playbackHandler;
    private Runnable playbackRunnable;
    private String currentPlayingPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recordings);

        // Check permissions
        checkPermissions();

        // Initialize recording manager
        recordingManager = new AudioRecordingManager(this);

        // Initialize UI components
        initUI();

        // Load and display recordings
        loadRecordings();
    }

    private void initUI() {
        Toolbar toolbar = findViewById(R.id.toolbarRecordings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerViewRecordings = findViewById(R.id.recyclerViewRecordings);
        layoutNoRecordings = findViewById(R.id.layoutNoRecordings);
        recyclerViewRecordings.setLayoutManager(new LinearLayoutManager(this));

        layoutPlaybackControl = findViewById(R.id.layoutPlaybackControl);
        textViewPlayingFile = findViewById(R.id.textViewPlayingFile);
        seekBarPlayback = findViewById(R.id.seekBarPlayback);
        textViewCurrentTime = findViewById(R.id.textViewCurrentTime);
        textViewTotalTime = findViewById(R.id.textViewTotalTime);
        buttonPlayPause = findViewById(R.id.buttonPlayPause);
        buttonStopPlayback = findViewById(R.id.buttonStopPlayback);
        buttonClosePlayback = findViewById(R.id.buttonClosePlayback);

        buttonPlayPause.setOnClickListener(v -> togglePlayback());
        buttonStopPlayback.setOnClickListener(v -> stopPlayback());
        buttonClosePlayback.setOnClickListener(v -> {
            stopPlayback();
            layoutPlaybackControl.setVisibility(View.GONE);
        });

        seekBarPlayback.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    updatePlaybackTime();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        playbackHandler = new Handler(Looper.getMainLooper());
        playbackRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    updatePlaybackTime();
                    playbackHandler.postDelayed(this, 1000); // Update every second
                }
            }
        };
    }

    private void loadRecordings() {
        recordings = recordingManager.getAllRecordings();

        if (recordings.isEmpty()) {
            recyclerViewRecordings.setVisibility(View.GONE);
            layoutNoRecordings.setVisibility(View.VISIBLE);
        } else {
            recyclerViewRecordings.setVisibility(View.VISIBLE);
            layoutNoRecordings.setVisibility(View.GONE);

            adapter = new RecordingsAdapter(recordings);
            recyclerViewRecordings.setAdapter(adapter);
        }
    }

    private void playRecording(String filePath, String displayName) {
        // Stop current playback if exists
        if (mediaPlayer != null) {
            stopPlayback();
        }

        try {
            // Start new playback
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                isPlaying = true;
                currentPlayingPath = filePath;

                // Update UI
                buttonPlayPause.setImageResource(R.drawable.ic_pause);
                layoutPlaybackControl.setVisibility(View.VISIBLE);
                textViewPlayingFile.setText(displayName);
                seekBarPlayback.setMax(mp.getDuration());
                textViewTotalTime.setText(formatTime(mp.getDuration()));

                // Start progress updates
                playbackHandler.post(playbackRunnable);
            });

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                buttonPlayPause.setImageResource(R.drawable.ic_play);
                seekBarPlayback.setProgress(0);
                textViewCurrentTime.setText("0:00");
                playbackHandler.removeCallbacks(playbackRunnable);
            });

            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Log.e(TAG, "Error playing recording: " + e.getMessage(), e);
            Toast.makeText(this, R.string.error_playing_recording, Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlayback() {
        if (mediaPlayer != null) {
            if (isPlaying) {
                mediaPlayer.pause();
                isPlaying = false;
                buttonPlayPause.setImageResource(R.drawable.ic_play);
                playbackHandler.removeCallbacks(playbackRunnable);
            } else {
                mediaPlayer.start();
                isPlaying = true;
                buttonPlayPause.setImageResource(R.drawable.ic_pause);
                playbackHandler.post(playbackRunnable);
            }
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            isPlaying = false;
            currentPlayingPath = null;
            playbackHandler.removeCallbacks(playbackRunnable);
            buttonPlayPause.setImageResource(R.drawable.ic_play);
            seekBarPlayback.setProgress(0);
            textViewCurrentTime.setText("0:00");
        }
    }

    private void updatePlaybackTime() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            seekBarPlayback.setProgress(currentPosition);
            textViewCurrentTime.setText(formatTime(currentPosition));
        }
    }

    private String formatTime(int millis) {
        int totalSeconds = millis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }

    private void showSendDialog(AudioRecording recording) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_send_analysis, null);
        builder.setView(dialogView);

        EditText editTextDescription = dialogView.findViewById(R.id.editTextSendDescription);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancelSend);
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirmSend);

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonConfirm.setOnClickListener(v -> {
            String description = editTextDescription.getText().toString().trim();
            if (description.isEmpty()) {
                Toast.makeText(MyRecordingsActivity.this, R.string.description_required, Toast.LENGTH_SHORT).show();
            } else {
                // Dismiss the dialog first, then process the send
                dialog.dismiss();
                simulateSendRecording(recording, description);
            }
        });
    }

    private void simulateSendRecording(AudioRecording recording, String description) {
        // Show sending message
        Toast.makeText(this, R.string.sending_recording, Toast.LENGTH_SHORT).show();

        // Simulate upload delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Add to sent recordings list
            sentRecordings.add(recording.getFilePath());

            // Notify item changed to update button state in the adapter
            int position = recordings.indexOf(recording);
            if (position != -1) {
                adapter.notifyItemChanged(position);
            }

            // Show success popup with custom layout
            showSendSuccessPopup();
        }, 1500);
    }

    private void showSendSuccessPopup() {
        // Create an AlertDialog with custom style
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle(R.string.recording_sent_title)
                .setMessage(R.string.recording_sent_success)
                .setIcon(R.drawable.ic_check_circle); // Make sure this icon exists

        // Use DialogInterface.OnClickListener instead of lambda for compatibility
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Auto-dismiss after short delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        }, 2000);
    }

    private void deleteRecording(AudioRecording recording) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle(R.string.delete_recording_title);
        builder.setMessage(R.string.delete_recording_message);

        // Use DialogInterface.OnClickListener instead of lambda for compatibility
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Check if the recording is currently playing
                if (currentPlayingPath != null && currentPlayingPath.equals(recording.getFilePath())) {
                    stopPlayback();
                    layoutPlaybackControl.setVisibility(View.GONE);
                }

                boolean deleted = recordingManager.deleteRecording(recording.getFilePath());
                if (deleted) {
                    // Remove from sent recordings list if it was there
                    sentRecordings.remove(recording.getFilePath());

                    int position = recordings.indexOf(recording);
                    if (position >= 0) {
                        recordings.remove(position);
                        adapter.notifyItemRemoved(position);

                        // Show empty state if no recordings left
                        if (recordings.isEmpty()) {
                            recyclerViewRecordings.setVisibility(View.GONE);
                            layoutNoRecordings.setVisibility(View.VISIBLE);
                        }
                    }
                    Toast.makeText(MyRecordingsActivity.this, R.string.recording_deleted, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MyRecordingsActivity.this, R.string.error_deleting_recording, Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void checkPermissions() {
        // Para Android 13+ precisamos de READ_MEDIA_AUDIO
        // Para Android 11+ precisamos de READ_EXTERNAL_STORAGE
        // Para Android 10 e anteriores, precisamos de READ_EXTERNAL_STORAGE e WRITE_EXTERNAL_STORAGE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                        REQUEST_PERMISSION_CODE);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_CODE);
            }
        } else {
            // Android 10 e anteriores
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        REQUEST_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {
                Toast.makeText(this, R.string.permissions_required, Toast.LENGTH_LONG).show();
                finish();
            } else {
                // Reload recordings now that we have permission
                loadRecordings();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isPlaying) {
            stopPlayback();
            layoutPlaybackControl.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        playbackHandler.removeCallbacks(playbackRunnable);
    }

    // RecyclerView Adapter
    private class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.RecordingViewHolder> {

        private List<AudioRecording> recordingsList;

        public RecordingsAdapter(List<AudioRecording> recordingsList) {
            this.recordingsList = recordingsList;
        }

        @NonNull
        @Override
        public RecordingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recording, parent, false);
            return new RecordingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecordingViewHolder holder, int position) {
            AudioRecording recording = recordingsList.get(position);
            holder.bind(recording);
        }

        @Override
        public int getItemCount() {
            return recordingsList.size();
        }

        class RecordingViewHolder extends RecyclerView.ViewHolder {
            private TextView textViewDate;
            private TextView textViewDuration;
            private TextView textViewSize;
            private ImageButton buttonPlay;
            private Button buttonSend;
            private Button buttonDelete;
            private AudioRecording recording;

            public RecordingViewHolder(@NonNull View itemView) {
                super(itemView);
                textViewDate = itemView.findViewById(R.id.textViewRecordingDate);
                textViewDuration = itemView.findViewById(R.id.textViewRecordingDuration);
                textViewSize = itemView.findViewById(R.id.textViewRecordingSize);
                buttonPlay = itemView.findViewById(R.id.buttonPlay);
                buttonSend = itemView.findViewById(R.id.buttonSendRecording);
                buttonDelete = itemView.findViewById(R.id.buttonDeleteRecording);

                buttonPlay.setOnClickListener(v -> {
                    playRecording(recording.getFilePath(), recording.getTimestamp());
                });

                buttonSend.setOnClickListener(v -> {
                    if (!sentRecordings.contains(recording.getFilePath())) {
                        showSendDialog(recording);
                    }
                });

                buttonDelete.setOnClickListener(v -> {
                    deleteRecording(recording);
                });
            }

            public void bind(AudioRecording recording) {
                this.recording = recording;
                textViewDate.setText(recording.getTimestamp());
                textViewDuration.setText(recording.getDuration());
                textViewSize.setText(recording.getFileSize());

                // Update play button state if this is the currently playing recording
                if (currentPlayingPath != null && currentPlayingPath.equals(recording.getFilePath()) && isPlaying) {
                    buttonPlay.setImageResource(R.drawable.ic_pause);
                } else {
                    buttonPlay.setImageResource(R.drawable.ic_play);
                }

                // Update send button based on whether this recording has been sent
                if (sentRecordings.contains(recording.getFilePath())) {
                    buttonSend.setText(R.string.recording_sent);
                    buttonSend.setEnabled(false);
                    buttonSend.setAlpha(0.7f);
                } else {
                    buttonSend.setText(R.string.send_for_analysis);
                    buttonSend.setEnabled(true);
                    buttonSend.setAlpha(1.0f);
                }
            }
        }
    }
}