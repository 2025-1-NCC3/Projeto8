package br.fecap.pi.ubersafestart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import br.fecap.pi.ubersafestart.R;
import br.fecap.pi.ubersafestart.utils.SafeScoreHelper;
import br.fecap.pi.ubersafestart.utils.AchievementTracker;


public class RideFeedbackActivity extends AppCompatActivity {

    private MaterialButton btnYes, btnNo, btnSendFeedback;
    private TextView txtReturnToMenu, txtRideCompleted, txtThanks;
    private EditText editTextFeedback;
    private boolean isRespected = false;
    private boolean selectionMade = false;
    private boolean isDriverMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_feedback);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isDriverMode = extras.getBoolean("IS_DRIVER_MODE", false);
        }

        // Inicializar views
        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);
        btnSendFeedback = findViewById(R.id.btnSendFeedback);
        txtReturnToMenu = findViewById(R.id.txtReturnToMenu);
        editTextFeedback = findViewById(R.id.editTextFeedback);
        txtRideCompleted = findViewById(R.id.txtRideCompleted);
        txtThanks = findViewById(R.id.txtThanks);

        // Personalizar a mensagem com base no modo (motorista ou passageiro)
        if (isDriverMode) {
            txtRideCompleted.setText("Corrida Finalizada!");
            txtThanks.setText("Obrigado por dirigir com o UberSafeStart!");
        }

        // Alterar o texto para "Ignorar"
        txtReturnToMenu.setText("Ignorar");
        txtReturnToMenu.setTextColor(ContextCompat.getColor(this, R.color.status_error));

        setupButtonListeners();
        updateButtonsAppearance();
    }

    private void setupButtonListeners() {
        btnYes.setOnClickListener(v -> {
            isRespected = true;
            selectionMade = true;
            updateButtonsAppearance();
        });

        btnNo.setOnClickListener(v -> {
            isRespected = false;
            selectionMade = true;
            updateButtonsAppearance();
        });

        btnSendFeedback.setOnClickListener(v -> {
            if (selectionMade) {
                String feedback = editTextFeedback.getText().toString().trim();

                saveFeedback(isRespected, feedback);

                // Atualiza o SafeScore usando o SafeScoreHelper - 10 pontos por dar feedback
                SafeScoreHelper.updateSafeScore(this, 10);

                AchievementTracker.trackAchievement(this, "feedback", 1);

                // Mostrar animação de sucesso
                showSuccessAnimation();

                Toast.makeText(this, "Feedback enviado! +10 pontos no SafeScore", Toast.LENGTH_SHORT).show();

                // Pequeno delay antes de retornar à tela inicial para mostrar a animação
                new android.os.Handler().postDelayed(() -> goToHomeScreen(), 800);
            } else {
                Toast.makeText(this, "Por favor, selecione Sim ou Não", Toast.LENGTH_SHORT).show();
            }
        });

        txtReturnToMenu.setOnClickListener(v -> {
            SafeScoreHelper.updateSafeScore(this, -5);
            Toast.makeText(this, "Feedback ignorado! -5 pontos de SafeScore.", Toast.LENGTH_SHORT).show();
            goToHomeScreen();
        });
    }

    private void updateButtonsAppearance() {
        if (isRespected) {
            // Selecionou "Sim"
            btnYes.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.uber_blue));
            btnYes.setStrokeColorResource(R.color.uber_blue);
            btnYes.setTextColor(ContextCompat.getColor(this, R.color.white_fff));

            btnNo.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.transparent));
            btnNo.setStrokeColorResource(R.color.gray_light);
            btnNo.setTextColor(ContextCompat.getColor(this, R.color.gray_light));
        } else if (selectionMade) {
            // Selecionou "Não"
            btnNo.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.uber_blue));
            btnNo.setStrokeColorResource(R.color.uber_blue);
            btnNo.setTextColor(ContextCompat.getColor(this, R.color.white_fff));

            btnYes.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.transparent));
            btnYes.setStrokeColorResource(R.color.gray_light);
            btnYes.setTextColor(ContextCompat.getColor(this, R.color.gray_light));
        } else {
            // Nenhuma seleção feita ainda
            btnYes.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.transparent));
            btnYes.setStrokeColorResource(R.color.gray_light);
            btnYes.setTextColor(ContextCompat.getColor(this, R.color.gray_light));

            btnNo.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.transparent));
            btnNo.setStrokeColorResource(R.color.gray_light);
            btnNo.setTextColor(ContextCompat.getColor(this, R.color.gray_light));
        }

        // Atualiza o botão de enviar feedback
        btnSendFeedback.setEnabled(selectionMade);
        if (selectionMade) {
            btnSendFeedback.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.uber_blue));
            btnSendFeedback.setTextColor(ContextCompat.getColor(this, R.color.white_fff));
        } else {
            btnSendFeedback.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_dark));
            btnSendFeedback.setTextColor(ContextCompat.getColor(this, R.color.gray_light));
        }
    }

    private void showSuccessAnimation() {
        btnSendFeedback.setEnabled(false);
        btnSendFeedback.setText("✓ Enviado");
        btnSendFeedback.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary_color));
    }

    private void saveFeedback(boolean isRespected, String comment) {
        System.out.println("Feedback: Respected = " + isRespected + ", Comment = " + comment);

        if (!isRespected) {
            System.out.println("Problema reportado: Usuário não se sentiu respeitado");
        }
    }

    private void goToHomeScreen() {
        Intent intent;
        if (isDriverMode) {
            intent = new Intent(RideFeedbackActivity.this, DriverHomeActivity.class);
        } else {
            intent = new Intent(RideFeedbackActivity.this, HomeActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }

    @Override
    public void onBackPressed() {
        SafeScoreHelper.updateSafeScore(this, -5);
        Toast.makeText(this, "Feedback ignorado! -5 pontos de SafeScore.", Toast.LENGTH_SHORT).show();
        goToHomeScreen();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}