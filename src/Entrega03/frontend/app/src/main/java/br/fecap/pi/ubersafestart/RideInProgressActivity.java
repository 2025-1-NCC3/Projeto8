package br.fecap.pi.ubersafestart;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
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

import com.bumptech.glide.Glide;
import br.fecap.pi.ubersafestart.R;
import br.fecap.pi.ubersafestart.utils.SafeScoreHelper;

public class RideInProgressActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_in_progress);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isDriverMode = extras.getBoolean("IS_DRIVER_MODE", false);
        }

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

        audioStatusIcon.setImageResource(R.drawable.ic_mic_off);
        audioStatusIcon.setColorFilter(Color.parseColor("#777777"));

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
        isReportActive = true;

        audioStatusIcon.setImageResource(R.drawable.ic_mic);
        audioStatusIcon.setColorFilter(Color.parseColor("#2979FF"));

        Toast.makeText(RideInProgressActivity.this,
                "Audio recording started",
                Toast.LENGTH_SHORT).show();
    }

    private void deactivateAudioRecording() {
        isReportActive = false;

        audioStatusIcon.setImageResource(R.drawable.ic_mic_off);
        audioStatusIcon.setColorFilter(Color.parseColor("#777777"));

        Toast.makeText(RideInProgressActivity.this,
                "Audio recording stopped",
                Toast.LENGTH_SHORT).show();
    }

    private void showReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

        if (reportCount == 0) {
            View reportView = getLayoutInflater().inflate(R.layout.dialog_report, null);
            Button btnStartRecording = reportView.findViewById(R.id.btnStartRecording);

            builder.setView(reportView);
            builder.setTitle("Report Problem");

            AlertDialog dialog = builder.create();

            btnStartRecording.setOnClickListener(v -> {
                reportCount++;

                isReportActive = true;
                btnStartRecording.setText("Recording activated");
                btnStartRecording.setEnabled(false);

                activateAudioRecording();

                dialog.dismiss();
            });

            dialog.show();
        } else {
            builder.setTitle("Cancel Ride");
            builder.setMessage("You are about to cancel the ride for safety reasons. Do you want to proceed?");
            builder.setPositiveButton("Cancel Ride", (dialog, which) -> {
                reportCount++;
                dialog.dismiss();
                cancelRideAndReturnToHome();
            });
            builder.setNegativeButton("Go Back", (dialog, which) -> {
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void cancelRideAndReturnToHome() {
        Toast.makeText(this, "Ride cancelled for safety reasons", Toast.LENGTH_SHORT).show();

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
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Cancel Ride")
                .setMessage("Do you really want to cancel this ride?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    cancelRideAndReturnToHome();
                })
                .setNegativeButton("No", null)
                .show();
    }
}