package br.fecap.pi.ubersafestart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.fecap.pi.ubersafestart.R;
import br.fecap.pi.ubersafestart.api.ApiClient;
import br.fecap.pi.ubersafestart.api.AuthService;
import br.fecap.pi.ubersafestart.model.LoginRequest;
import br.fecap.pi.ubersafestart.model.LoginResponse;
import br.fecap.pi.ubersafestart.utils.SafeScoreHelper;
import br.fecap.pi.ubersafestart.model.ProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmailLogin);
        editTextPassword = findViewById(R.id.editTextPasswordLogin);
        buttonLogin = findViewById(R.id.buttonLogin);

        TextView textViewSignUpLink = findViewById(R.id.textViewSignUp);
        textViewSignUpLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        authService = ApiClient.getClient().create(AuthService.class);

        buttonLogin.setOnClickListener(view -> loginUser());

        TextView textViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        textViewForgotPassword.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Tentando login com email: " + email);

        LoginRequest request = new LoginRequest(email, password);
        authService.loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();

                    // IMPORTANTE: Limpar conquistas locais ao fazer login com nova conta
                    SharedPreferences completedPrefs = getSharedPreferences("CompletedAchievements", MODE_PRIVATE);
                    completedPrefs.edit().clear().apply();

                    SharedPreferences prefs = getSharedPreferences("userPrefs", MODE_PRIVATE);
                    prefs.edit().putString("token", token).apply();

                    authService.getProfile("Bearer " + token).enqueue(new Callback<ProfileResponse>() {
                        @Override
                        public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                ProfileResponse profile = response.body();

                                SafeScoreHelper.saveAuthData(
                                        LoginActivity.this,
                                        token,
                                        profile.getUsername(),
                                        profile.getEmail(),
                                        profile.getType(),
                                        profile.getPhone()
                                );

                                Toast.makeText(LoginActivity.this, "Login bem-sucedido", Toast.LENGTH_SHORT).show();

                                Intent intent;
                                if ("driver".equalsIgnoreCase(profile.getType())) {
                                    intent = new Intent(LoginActivity.this, DriverHomeActivity.class);
                                } else {
                                    intent = new Intent(LoginActivity.this, HomeActivity.class);
                                }

                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                            } else {
                                // Mesmo com falha, podemos prosseguir com os dados básicos
                                Toast.makeText(LoginActivity.this, "Login bem-sucedido, mas falha ao carregar perfil",
                                        Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                            }
                        }

                        @Override
                        public void onFailure(Call<ProfileResponse> call, Throwable t) {
                            Toast.makeText(LoginActivity.this, "Login bem-sucedido, mas falha de conexão",
                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                        }
                    });
                } else {
                    Toast.makeText(LoginActivity.this, "Erro no login", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}