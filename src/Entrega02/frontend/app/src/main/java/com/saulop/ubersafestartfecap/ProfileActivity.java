package com.saulop.ubersafestartfecap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
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
        Intent intent = getIntent();
        String userType = intent.getStringExtra("USER_TYPE");
        String userName = intent.getStringExtra("USER_NAME");

        if (userType == null) userType = "passenger";
        if (userName == null) userName = "Usuário";

        if (userType.equals("driver")) {
            textViewName.setText(userName);
            textViewEmail.setText("joao.silva@email.com");
            textViewPhone.setText("(11) 98765-4321");
            textViewAccountType.setText("Motorista");
            textViewRating.setText("4.8");
            textViewSafeScore.setText("95/100");
            progressBarSafeScore.setProgress(95);
        } else {
            textViewName.setText(userName);
            textViewEmail.setText("maria.santos@email.com");
            textViewPhone.setText("(11) 98765-4321");
            textViewAccountType.setText("Passageiro");
            textViewRating.setText("4.9");
            textViewSafeScore.setText("90/100");
            progressBarSafeScore.setProgress(90);
        }
    }

    private void setupClickListeners() {
        buttonEditProfile.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Editar perfil em breve", Toast.LENGTH_SHORT).show();
        });

        buttonSafetySettings.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Configurações de segurança em breve", Toast.LENGTH_SHORT).show();
        });

        buttonLogout.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Saindo...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        navHome.setOnClickListener(v -> {
            String userType = getIntent().getStringExtra("USER_TYPE");
            if (userType != null && userType.equals("driver")) {
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