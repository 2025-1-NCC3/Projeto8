package br.fecap.pi.ubersafestart;

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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import br.fecap.pi.ubersafestart.R;

import br.fecap.pi.ubersafestart.api.ApiClient;
import br.fecap.pi.ubersafestart.api.AuthService;
import br.fecap.pi.ubersafestart.model.ApiResponse;
import br.fecap.pi.ubersafestart.model.SafeScoreResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private TextView textViewName;
    private TextView textViewEmail;
    private TextView textViewPhone;
    private TextView textViewAccountType;
    private TextView textViewRating;
    private TextView textViewSafeScore;
    private ProgressBar progressBarSafeScore;

    private Button buttonLogout;
    private Button buttonEditProfile;
    private Button buttonDeleteAccount;
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
        textViewRating = findViewById(R.id.textViewRating);
        textViewSafeScore = findViewById(R.id.textViewSafeScore);
        progressBarSafeScore = findViewById(R.id.progressBarSafeScore);

        buttonLogout = findViewById(R.id.buttonLogout);
        buttonEditProfile = findViewById(R.id.buttonEditProfile);
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);
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

        buttonDeleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteAccountConfirmation();
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

    private void showDeleteAccountConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Deletar Conta");
        builder.setMessage("Você tem certeza que deseja deletar sua conta? Esta ação não pode ser desfeita.");

        builder.setPositiveButton("Sim", (dialog, which) -> {
            deleteUserAccount();
        });

        builder.setNegativeButton("Não", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteUserAccount() {
        SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
        String token = prefs.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(this, "Você não está logado", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show a loading indicator
        Toast.makeText(this, "Processando solicitação...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Iniciando requisição para deletar conta");

        // Ensure token format is correct
        String bearerToken = token;
        if (!token.startsWith("Bearer ")) {
            bearerToken = "Bearer " + token;
        }

        // Log request for debugging (mask most of the token)
        String maskedToken = bearerToken.length() > 15 ?
                bearerToken.substring(0, 10) + "..." : "[token vazio ou inválido]";
        Log.d(TAG, "Token: " + maskedToken);

        authService.deleteUser(bearerToken).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                Log.d(TAG, "Código de resposta: " + response.code());

                if (response.isSuccessful()) {
                    ApiResponse body = response.body();
                    if (body != null) {
                        Log.d(TAG, "Resposta: " + body.getMessage());
                        if (body.isSuccess()) {
                            Toast.makeText(ProfileActivity.this, "Conta deletada com sucesso", Toast.LENGTH_SHORT).show();

                            // Clear user preferences
                            prefs.edit().clear().apply();

                            // Navigate back to SignUp screen
                            Intent intent = new Intent(ProfileActivity.this, SignUpActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            // API returned success:false
                            String errorMsg = body.getMessage();
                            Log.e(TAG, "Erro na deleção: " + errorMsg);
                            Toast.makeText(ProfileActivity.this, "Falha ao deletar conta: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // Response body is null
                        Log.e(TAG, "Resposta vazia do servidor");
                        Toast.makeText(ProfileActivity.this, "Falha ao deletar conta: Resposta vazia do servidor", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Response not successful
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "desconhecido";
                        Log.e(TAG, "Erro HTTP " + response.code() + ": " + errorBody);
                        Toast.makeText(ProfileActivity.this, "Falha ao deletar conta: Erro HTTP " + response.code(), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler errorBody", e);
                        Toast.makeText(ProfileActivity.this, "Falha ao deletar conta: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Falha na requisição de deleção: ", t);
                Toast.makeText(ProfileActivity.this, "Erro de conexão: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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
        textViewRating.setText("4.8");

        // Added SafeScore display from local storage
        int safeScore = prefs.getInt("safescore", 0);
        textViewSafeScore.setText(String.valueOf(safeScore) + "/100");
        progressBarSafeScore.setProgress(safeScore);

        // Fetch SafeScore from server
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
    }
}