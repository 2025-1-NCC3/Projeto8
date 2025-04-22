package com.saulop.ubersafestartfecap;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.saulop.ubersafestartfecap.api.ApiClient;
import com.saulop.ubersafestartfecap.api.AuthService;
import com.saulop.ubersafestartfecap.model.ProfileResponse;
import com.saulop.ubersafestartfecap.model.SafeScoreResponse;
import com.saulop.ubersafestartfecap.utils.SafeScoreHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private TextView textViewName;
    private TextView textViewEmail;
    private TextView textViewPhone;
    private TextView textViewAccountType;
    private TextView textViewSafeScore;
    private TextView textViewRating;

    private ProgressBar progressBarSafeScore;

    private Button buttonLogout;
    private Button buttonEditProfile;
    private Button buttonSafetySettings;

    private LinearLayout navHome;
    private LinearLayout navServices;
    private LinearLayout navActivity;
    private LinearLayout navAccount;

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewAccountType = findViewById(R.id.textViewAccountType);
        textViewSafeScore = findViewById(R.id.textViewSafeScore);
        textViewRating = findViewById(R.id.textViewRating);

        progressBarSafeScore = findViewById(R.id.progressBarSafeScore);

        buttonLogout = findViewById(R.id.buttonLogout);
        buttonEditProfile = findViewById(R.id.buttonEditProfile);
        buttonSafetySettings = findViewById(R.id.buttonSafetySettings);

        navHome = findViewById(R.id.navHome);
        navServices = findViewById(R.id.navServices);
        navActivity = findViewById(R.id.navActivity);
        navAccount = findViewById(R.id.navAccount);

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
                prefs.edit().clear().apply();

                // Return to login screen
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        buttonEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Função de edição em desenvolvimento", Toast.LENGTH_SHORT).show();
            }
        });

        buttonSafetySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Configurações de segurança em desenvolvimento", Toast.LENGTH_SHORT).show();
            }
        });

        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
                String userType = prefs.getString("type", "");

                Intent intent;
                if ("driver".equalsIgnoreCase(userType)) {
                    intent = new Intent(ProfileActivity.this, DriverHomeActivity.class);
                } else {
                    intent = new Intent(ProfileActivity.this, HomeActivity.class);
                }

                startActivity(intent);
                finish();
            }
        });

        navServices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Opções em desenvolvimento", Toast.LENGTH_SHORT).show();
            }
        });

        navActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ProfileActivity.this, "Atividade em desenvolvimento", Toast.LENGTH_SHORT).show();
            }
        });

        authService = ApiClient.getClient().create(AuthService.class);

        loadProfileData();
    }

    private void loadProfileData() {
        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(this, "Você não está logado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textViewName.setText(prefs.getString("username", ""));
        textViewEmail.setText(prefs.getString("email", ""));
        textViewPhone.setText(prefs.getString("phone", ""));
        textViewAccountType.setText(prefs.getString("type", ""));

        int safeScore = prefs.getInt("safescore", 0);
        textViewSafeScore.setText(String.valueOf(safeScore) + "/100");
        progressBarSafeScore.setProgress(safeScore);

        textViewRating.setText("4.8");

        authService.getSafeScore("Bearer " + token).enqueue(new Callback<SafeScoreResponse>() {
            @Override
            public void onResponse(Call<SafeScoreResponse> call, Response<SafeScoreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int safescore = response.body().getSafescore();
                    textViewSafeScore.setText(String.valueOf(safescore) + "/100");
                    progressBarSafeScore.setProgress(safescore);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("safescore", safescore);
                    editor.apply();

                    Log.d(TAG, "SafeScore atualizado do servidor: " + safescore);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "desconhecido";
                        Log.e(TAG, "Erro ao buscar SafeScore: " + errorBody);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler errorBody", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<SafeScoreResponse> call, Throwable t) {
                Log.e(TAG, "Falha ao buscar SafeScore: " + t.getMessage());
            }
        });

        authService.getSafeScore("Bearer " + token).enqueue(new Callback<SafeScoreResponse>() {
            @Override
            public void onResponse(Call<SafeScoreResponse> call, Response<SafeScoreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int safescore = response.body().getSafescore();
                    textViewSafeScore.setText(String.valueOf(safescore) + "/100");
                    progressBarSafeScore.setProgress(safescore);

                    prefs.edit().putInt("safescore", safescore).apply();
                    Log.d(TAG, "SafeScore atualizado: " + safescore);
                }
            }

            @Override
            public void onFailure(Call<SafeScoreResponse> call, Throwable t) {
                Log.e(TAG, "Falha ao buscar SafeScore: " + t.getMessage());
            }
        });
    }
}