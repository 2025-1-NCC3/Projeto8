package br.fecap.pi.ubersafestart;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import br.fecap.pi.ubersafestart.utils.AudioRecordingManager;
import br.fecap.pi.ubersafestart.utils.SafeScoreHelper;
import br.fecap.pi.ubersafestart.utils.AchievementTracker;

public class RideInProgressActivity extends AppCompatActivity {

    private static final String TAG = "RideInProgressActivity";
    private static final int REQUEST_PERMISSION_CODE = 1000;

    private TextView timerTextView;
    private TextView statusTextView;
    private ProgressBar timerProgressBar;
    private ImageView reportButton;
    private ImageView audioStatusIcon;
    private ImageView carImageView;
    private CountDownTimer countDownTimer;
    private ValueAnimator progressAnimator;
    private boolean isReportActive = false;
    private int reportCount = 0;
    private final long TIMER_DURATION = 30000;
    private final long TIMER_INTERVAL = 1000;
    private final long MIDWAY_CHECKPOINT = 15000;
    private boolean isDriverMode = false;
    private PopupWindow popupWindow;

    // Gravação de áudio
    private AudioRecordingManager audioRecordingManager;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_in_progress);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isDriverMode = extras.getBoolean("IS_DRIVER_MODE", false);
        }

        // Inicializa o gerenciador de gravação de áudio
        audioRecordingManager = new AudioRecordingManager(this);

        timerTextView = findViewById(R.id.timerTextView);
        timerProgressBar = findViewById(R.id.timerProgressBar);
        reportButton = findViewById(R.id.reportButton);
        audioStatusIcon = findViewById(R.id.audioStatusIcon);
        carImageView = findViewById(R.id.carImageView);
        statusTextView = findViewById(R.id.statusTextView);

        Glide.with(this)
                .asGif()
                .load(R.drawable.car_animated)
                .into(carImageView);

        // Inicialmente, a gravação de áudio está desativada
        audioStatusIcon.setImageResource(R.drawable.ic_mic_off);
        audioStatusIcon.setColorFilter(Color.parseColor("#777777"));

        // Adiciona listener ao ícone de áudio para ativar/desativar a gravação manualmente
        audioStatusIcon.setOnClickListener(v -> {
            if (checkAudioPermission()) {
                toggleAudioRecording();
            } else {
                requestAudioPermission();
            }
        });

        reportButton.setOnClickListener(v -> showReportDialog());

        timerProgressBar.setMax((int) TIMER_DURATION);
        timerProgressBar.setProgress((int) TIMER_DURATION);

        startRideTimer();
    }

    private void startRideTimer() {
        countDownTimer = new CountDownTimer(TIMER_DURATION, TIMER_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                timerTextView.setText(seconds + "s");

                updateProgressBarSmoothly(millisUntilFinished);

                if (millisUntilFinished <= MIDWAY_CHECKPOINT &&
                        millisUntilFinished > MIDWAY_CHECKPOINT - TIMER_INTERVAL) {
                    showMidwayCheckPopup();
                }
            }

            @Override
            public void onFinish() {
                timerTextView.setText("0s");
                timerProgressBar.setProgress(0);

                finishRideAndShowFeedback();
            }
        }.start();
    }

    private void updateProgressBarSmoothly(long millisUntilFinished) {
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }

        int currentProgress = timerProgressBar.getProgress();
        int targetProgress = (int) millisUntilFinished;

        progressAnimator = ValueAnimator.ofInt(currentProgress, targetProgress);
        progressAnimator.setDuration(900);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            timerProgressBar.setProgress(animatedValue);
        });
        progressAnimator.start();
    }

    private void showMidwayCheckPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_midway_check_dialog, null);

        int width = (int)(getResources().getDisplayMetrics().widthPixels * 0.85);

        popupWindow = new PopupWindow(
                popupView,
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        View rootView = getWindow().getDecorView().getRootView();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha = 0.8f;
        getWindow().setAttributes(params);

        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams dismissParams = getWindow().getAttributes();
            dismissParams.alpha = 1f;
            getWindow().setAttributes(dismissParams);
        });

        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

        Button btnOk = popupView.findViewById(R.id.btnOk);
        Button btnProblem = popupView.findViewById(R.id.btnProblem);

        btnOk.setOnClickListener(v -> {
            SafeScoreHelper.updateSafeScore(RideInProgressActivity.this, 5);
            popupWindow.dismiss();
        });

        btnProblem.setOnClickListener(v -> {
            popupWindow.dismiss();
            showReportDialog();
        });

        popupWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);
    }

    private void activateAudioRecording() {
        // Verifica permissão primeiro
        if (!checkAudioPermission()) {
            requestAudioPermission();
            return;
        }

        // Inicia a gravação real
        boolean success = audioRecordingManager.startRecording();
        if (success) {
            isRecording = true;
            isReportActive = true;

            // Atualiza a UI
            audioStatusIcon.setImageResource(R.drawable.ic_mic);
            audioStatusIcon.setColorFilter(Color.parseColor("#2979FF"));

            // Rastreia a conquista de uso de áudio
            AchievementTracker.trackAchievement(RideInProgressActivity.this, "audio", 1);

            Toast.makeText(RideInProgressActivity.this,
                    "Gravação de áudio iniciada",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Falha ao iniciar a gravação de áudio", Toast.LENGTH_SHORT).show();
        }
    }

    private void deactivateAudioRecording() {
        if (isRecording) {
            String recordingPath = audioRecordingManager.stopRecording();
            if (recordingPath != null) {
                Log.d(TAG, "Gravação salva em: " + recordingPath);
                Toast.makeText(this, "Gravação salva", Toast.LENGTH_SHORT).show();
            }

            isRecording = false;
            isReportActive = false;

            // Atualiza a UI
            audioStatusIcon.setImageResource(R.drawable.ic_mic_off);
            audioStatusIcon.setColorFilter(Color.parseColor("#777777"));
        }
    }

    private void toggleAudioRecording() {
        if (isRecording) {
            deactivateAudioRecording();
            Toast.makeText(this, "Gravação de áudio interrompida", Toast.LENGTH_SHORT).show();
        } else {
            activateAudioRecording();
        }
    }

    private void showReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

        if (reportCount == 0) {
            View reportView = getLayoutInflater().inflate(R.layout.dialog_report, null);
            Button btnStartRecording = reportView.findViewById(R.id.btnStartRecording);

            builder.setView(reportView);
            builder.setTitle("Reportar Problema");

            AlertDialog dialog = builder.create();

            btnStartRecording.setOnClickListener(v -> {
                reportCount++;

                // Inicia a gravação real de áudio
                activateAudioRecording();

                btnStartRecording.setText("Gravação ativada");
                btnStartRecording.setEnabled(false);

                dialog.dismiss();
            });

            dialog.show();
        } else {
            builder.setTitle("Cancelar Corrida");
            builder.setMessage("Você está prestes a cancelar a corrida por motivos de segurança. Deseja prosseguir?");
            builder.setPositiveButton("Cancelar Corrida", (dialog, which) -> {
                reportCount++;
                dialog.dismiss();
                cancelRideAndReturnToHome();
            });
            builder.setNegativeButton("Voltar", (dialog, which) -> {
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void cancelRideAndReturnToHome() {
        // Certifique-se de parar qualquer gravação em andamento antes de sair
        if (isRecording) {
            deactivateAudioRecording();
        }

        Toast.makeText(this, "Corrida cancelada por motivos de segurança", Toast.LENGTH_SHORT).show();

        Intent intent;
        if (isDriverMode) {
            intent = new Intent(RideInProgressActivity.this, DriverHomeActivity.class);
        } else {
            intent = new Intent(RideInProgressActivity.this, HomeActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    private void finishRideAndShowFeedback() {
        // Certifique-se de parar qualquer gravação em andamento antes de sair
        if (isRecording) {
            deactivateAudioRecording();
        }

        // Registra a conquista de viagem
        AchievementTracker.trackAchievement(RideInProgressActivity.this, "trip", 1);

        Intent homeIntent;
        if (isDriverMode) {
            homeIntent = new Intent(RideInProgressActivity.this, DriverHomeActivity.class);
        } else {
            homeIntent = new Intent(RideInProgressActivity.this, HomeActivity.class);
        }
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

        Intent feedbackIntent = new Intent(RideInProgressActivity.this, RideFeedbackActivity.class);
        feedbackIntent.putExtra("IS_DRIVER_MODE", isDriverMode);
        startActivity(feedbackIntent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
        // Certifique-se de parar a gravação de áudio
        if (isRecording) {
            deactivateAudioRecording();
        }
    }

    // Métodos para gestão de permissões
    private boolean checkAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            int storageResult = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED &&
                    (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                            storageResult == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    private void requestAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_PERMISSION_CODE);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida, ativa a gravação
                toggleAudioRecording();
            } else {
                Toast.makeText(this, "Permissão de gravação negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Cancelar Corrida")
                .setMessage("Você realmente deseja cancelar esta corrida?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    // Para qualquer gravação em andamento
                    if (isRecording) {
                        deactivateAudioRecording();
                    }
                    cancelRideAndReturnToHome();
                })
                .setNegativeButton("Não", null)
                .show();
    }
}