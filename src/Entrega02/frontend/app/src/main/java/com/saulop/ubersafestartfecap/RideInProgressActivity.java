package com.saulop.ubersafestartfecap;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.saulop.ubersafestartfecap.utils.SafeScoreHelper;

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
    private final long TIMER_DURATION = 30000; // 30 segundos
    private final long TIMER_INTERVAL = 1000; // Intervalo de 1 segundo
    private final long MIDWAY_CHECKPOINT = 15000; // 15 segundos
    private boolean isDriverMode = false;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_in_progress);

        // Checar se é modo motorista ou passageiro (você pode passar isso via Intent)
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isDriverMode = extras.getBoolean("IS_DRIVER_MODE", false);
        }

        // Inicializar views
        timerTextView = findViewById(R.id.timerTextView);
        timerProgressBar = findViewById(R.id.timerProgressBar);
        reportButton = findViewById(R.id.reportButton);
        audioStatusIcon = findViewById(R.id.audioStatusIcon);
        carImageView = findViewById(R.id.carImageView);
        statusTextView = findViewById(R.id.statusTextView);

        // Carregar e animar o GIF do carro usando Glide
        Glide.with(this)
                .asGif()
                .load(R.drawable.car_animated)
                .into(carImageView);

        // Configurar o ícone de áudio como mutado inicialmente
        audioStatusIcon.setImageResource(R.drawable.ic_mic_off);
        audioStatusIcon.setColorFilter(Color.parseColor("#777777"));

        // Configurar o listener do botão de report
        reportButton.setOnClickListener(v -> showReportDialog());

        // Configurar a barra de progresso
        timerProgressBar.setMax((int) TIMER_DURATION);
        timerProgressBar.setProgress((int) TIMER_DURATION);

        // Iniciar o timer de contagem regressiva
        startRideTimer();
    }

    private void startRideTimer() {
        countDownTimer = new CountDownTimer(TIMER_DURATION, TIMER_INTERVAL) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Atualizar texto do timer
                int seconds = (int) (millisUntilFinished / 1000);
                timerTextView.setText(seconds + "s");

                // Animar a barra de progresso suavemente
                updateProgressBarSmoothly(millisUntilFinished);

                // Verificar se estamos no ponto médio para mostrar o diálogo de checagem
                if (millisUntilFinished <= MIDWAY_CHECKPOINT &&
                        millisUntilFinished > MIDWAY_CHECKPOINT - TIMER_INTERVAL) {
                    showMidwayCheckPopup();
                }
            }

            @Override
            public void onFinish() {
                timerTextView.setText("0s");
                timerProgressBar.setProgress(0);

                // Imediatamente retornar ao menu principal e iniciar a tela de feedback
                finishRideAndShowFeedback();
            }
        }.start();
    }

    private void updateProgressBarSmoothly(long millisUntilFinished) {
        // Cancelar animador anterior se existir
        if (progressAnimator != null) {
            progressAnimator.cancel();
        }

        // Valor atual da barra de progresso
        int currentProgress = timerProgressBar.getProgress();
        // Valor alvo para a próxima atualização
        int targetProgress = (int) millisUntilFinished;

        // Criar e iniciar um animador para transição suave
        progressAnimator = ValueAnimator.ofInt(currentProgress, targetProgress);
        progressAnimator.setDuration(900); // Um pouco menos que o intervalo para garantir que termine antes da próxima atualização
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            timerProgressBar.setProgress(animatedValue);
        });
        progressAnimator.start();
    }

    private void showMidwayCheckPopup() {
        // Inflar o layout personalizado
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.activity_midway_check_dialog, null);

        // Criar o popup window
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        // Configurar animação
        popupWindow.setAnimationStyle(android.R.style.Animation_Dialog);

        // Configurar botões
        Button btnOk = popupView.findViewById(R.id.btnOk);
        Button btnProblem = popupView.findViewById(R.id.btnProblem);

        btnOk.setOnClickListener(v -> {
            // Usuário confirma que está tudo bem
            // Atualizar o SafeScore
            SafeScoreHelper.updateSafeScore(RideInProgressActivity.this, 5);
            popupWindow.dismiss();
        });

        btnProblem.setOnClickListener(v -> {
            // Usuário tem um problema, mostrar diálogo de report
            popupWindow.dismiss();
            showReportDialog();
        });

        // Mostrar o popup abaixo da mensagem "Viagem em andamento..."
        popupWindow.showAsDropDown(statusTextView, 0, 20, Gravity.CENTER);
    }

    private void showReportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);

        if (reportCount == 0) {
            // Primeiro report - mostrar diálogo de gravação
            View reportView = getLayoutInflater().inflate(R.layout.dialog_report, null);
            Button btnStartRecording = reportView.findViewById(R.id.btnStartRecording);

            builder.setView(reportView);
            builder.setTitle("Reportar Problema");

            AlertDialog dialog = builder.create();

            btnStartRecording.setOnClickListener(v -> {
                reportCount++;

                // Ativar gravação
                isReportActive = true;
                btnStartRecording.setText("Gravação ativada");
                btnStartRecording.setEnabled(false);

                // Atualizar ícone de áudio para indicar gravação ativa
                audioStatusIcon.setImageResource(R.drawable.ic_mic);
                audioStatusIcon.setColorFilter(Color.parseColor("#00C853")); // Verde

                Toast.makeText(RideInProgressActivity.this,
                        "Gravação de áudio iniciada",
                        Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            dialog.show();
        } else {
            // Segundo report - mostrar diálogo de confirmação de cancelamento
            builder.setTitle("Cancelar Viagem");
            builder.setMessage("Você está prestes a cancelar a viagem por motivos de segurança. Deseja prosseguir?");
            builder.setPositiveButton("Cancelar Viagem", (dialog, which) -> {
                reportCount++;
                dialog.dismiss();
                cancelRideAndReturnToHome();  // Chama método que cancela a viagem
            });
            builder.setNegativeButton("Voltar", (dialog, which) -> {
                // Apenas fecha o diálogo
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void cancelRideAndReturnToHome() {
        // Breve mensagem Toast para feedback visual
        Toast.makeText(this, "Viagem cancelada por motivos de segurança", Toast.LENGTH_SHORT).show();

        // Retornar à tela inicial apropriada
        Intent intent;
        if (isDriverMode) {
            intent = new Intent(RideInProgressActivity.this, DriverHomeActivity.class);
        } else {
            intent = new Intent(RideInProgressActivity.this, HomeActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    // Novo método para finalizar a viagem e mostrar a tela de feedback
    private void finishRideAndShowFeedback() {
        Intent homeIntent;
        if (isDriverMode) {
            homeIntent = new Intent(RideInProgressActivity.this, DriverHomeActivity.class);
        } else {
            homeIntent = new Intent(RideInProgressActivity.this, HomeActivity.class);
        }
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);

        Intent feedbackIntent = new Intent(RideInProgressActivity.this, RideFeedbackActivity.class);
        feedbackIntent.putExtra("IS_DRIVER_MODE", isDriverMode);
        startActivity(feedbackIntent);

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
}