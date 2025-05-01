package br.fecap.pi.ubersafestart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.fecap.pi.ubersafestart.R;
import br.fecap.pi.ubersafestart.utils.SafeScoreHelper;

public class RideFeedbackActivity extends AppCompatActivity {

    private Button btnYes, btnNo, btnSendFeedback;
    private TextView txtReturnToMenu;
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

        btnYes = findViewById(R.id.btnYes);
        btnNo = findViewById(R.id.btnNo);
        btnSendFeedback = findViewById(R.id.btnSendFeedback);
        txtReturnToMenu = findViewById(R.id.txtReturnToMenu);
        editTextFeedback = findViewById(R.id.editTextFeedback);

        // Alterar o texto para "Ignorar"
        txtReturnToMenu.setText("Ignorar");
        txtReturnToMenu.setTextColor(getResources().getColor(android.R.color.holo_red_light));

        setupButtonListeners();
    }

    private void setupButtonListeners() {
        btnYes.setOnClickListener(v -> {
            btnYes.setBackgroundResource(R.drawable.button_primary_selected);
            btnNo.setBackgroundResource(R.drawable.button_secondary);
            isRespected = true;
            selectionMade = true;
        });

        btnNo.setOnClickListener(v -> {
            btnNo.setBackgroundResource(R.drawable.button_secondary_selected);
            btnYes.setBackgroundResource(R.drawable.button_primary);
            isRespected = false;
            selectionMade = true;
        });

        btnSendFeedback.setOnClickListener(v -> {
            if (selectionMade) {
                String feedback = editTextFeedback.getText().toString().trim();

                saveFeedback(isRespected, feedback);

                // Atualiza o SafeScore usando o SafeScoreHelper - 10 pontos por dar feedback
                SafeScoreHelper.updateSafeScore(this, 10);

                Toast.makeText(this, "Feedback enviado!", Toast.LENGTH_SHORT).show();

                goToHomeScreen();
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
        // Adiciona animação de transição ao voltar para a tela inicial
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