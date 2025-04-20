package com.saulop.ubersafestartfecap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView textViewName, textViewEmail, textViewPhone, textViewAccountType, textViewSafeScore, textViewRating;
    private ProgressBar progressBarSafeScore;
    private Button buttonEditProfile, buttonSafetySettings, buttonLogout;
    private LinearLayout navHome, navServices, navActivity, navAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        loadUserData();
        setupClickListeners();
    }

    private void initViews() {
        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewAccountType = findViewById(R.id.textViewAccountType);
        textViewSafeScore = findViewById(R.id.textViewSafeScore);
        textViewRating = findViewById(R.id.textViewRating);
        progressBarSafeScore = findViewById(R.id.progressBarSafeScore);

        buttonEditProfile = findViewById(R.id.buttonEditProfile);
        buttonSafetySettings = findViewById(R.id.buttonSafetySettings);
        buttonLogout = findViewById(R.id.buttonLogout);

        navHome = findViewById(R.id.navHome);
        navServices = findViewById(R.id.navServices);
        navActivity = findViewById(R.id.navActivity);
        navAccount = findViewById(R.id.navAccount);
    }

    private void loadUserData() {
        // Recuperar dados do SharedPreferences
        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "Usuário");
        String email = prefs.getString("email", "email@exemplo.com");
        String phone = prefs.getString("phone", "Não informado");
        String userType = prefs.getString("type", "passenger");

        // Definir os dados nos TextViews
        textViewName.setText(username);
        textViewEmail.setText(email);
        textViewPhone.setText(phone);
        textViewAccountType.setText(userType.equals("driver") ? "Motorista" : "Passageiro");

        // Definir valores mockados para SafeScore e Rating (ajuste conforme necessário)
        int safeScore = userType.equals("driver") ? 95 : 90;
        float rating = userType.equals("driver") ? 4.8f : 4.9f;

        textViewSafeScore.setText(safeScore + "/100");
        textViewRating.setText(String.valueOf(rating));
        progressBarSafeScore.setProgress(safeScore);
    }

    private void setupClickListeners() {
        buttonEditProfile.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Editar perfil em breve", Toast.LENGTH_SHORT).show();
        });

        buttonSafetySettings.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Configurações de segurança em breve", Toast.LENGTH_SHORT).show();
        });

        buttonLogout.setOnClickListener(v -> {
            // Limpar SharedPreferences ao fazer logout
            SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(ProfileActivity.this, "Saindo...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        navHome.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
            String userType = prefs.getString("type", "passenger");
            if (userType.equals("driver")) {
                startActivity(new Intent(ProfileActivity.this, DriverHomeActivity.class));
            } else {
                startActivity(new Intent(ProfileActivity.this, HomeActivity.class));
            }
            finish();
        });

        navServices.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Opções em breve", Toast.LENGTH_SHORT).show();
        });

        navActivity.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Atividade em breve", Toast.LENGTH_SHORT).show();
        });

        navAccount.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Você já está no perfil", Toast.LENGTH_SHORT).show();
        });
    }
}