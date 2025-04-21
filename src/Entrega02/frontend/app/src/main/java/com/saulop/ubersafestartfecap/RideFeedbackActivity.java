package com.saulop.ubersafestartfecap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RideFeedbackActivity extends AppCompatActivity {

    private Button btnYes, btnNo, btnSendFeedback;
    private TextView txtReturnToMenu;
    private EditText editTextFeedback;
    private boolean isRespected = false;
    private boolean selectionMade = false;
    private boolean isDriverMode = false;
    private static final String PREFS_NAME = "SafeScorePrefs";
    private static final String SAFE_SCORE_KEY = "safeScore";

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

                updateSafeScore(5);

                Toast.makeText(this, "Feedback enviado!", Toast.LENGTH_SHORT).show();

                goToHomeScreen();
            } else {
                Toast.makeText(this, "Por favor, selecione Sim ou Não", Toast.LENGTH_SHORT).show();
            }
        });

        txtReturnToMenu.setOnClickListener(v -> {
            goToHomeScreen();
        });
    }

    private void saveFeedback(boolean isRespected, String comment) {
        System.out.println("Feedback: Respected = " + isRespected + ", Comment = " + comment);
    }

    private void updateSafeScore(int points) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int currentScore = prefs.getInt(SAFE_SCORE_KEY, 0);

        int newScore = currentScore + points;

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(SAFE_SCORE_KEY, newScore);
        editor.apply();
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
        finish();
    }

    @Override
    public void onBackPressed() {
        goToHomeScreen();
    }
}