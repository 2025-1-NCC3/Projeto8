package com.saulop.ubersafestartfecap;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.saulop.ubersafestartfecap.api.ApiClient;
import com.saulop.ubersafestartfecap.api.AuthService;
import com.saulop.ubersafestartfecap.model.LoginRequest;
import com.saulop.ubersafestartfecap.model.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Componentes de login
        editTextEmail = findViewById(R.id.editTextEmailLogin);
        editTextPassword = findViewById(R.id.editTextPasswordLogin);
        buttonLogin = findViewById(R.id.buttonLogin);

        // Configura o link de cadastro
        TextView textViewSignUpLink = findViewById(R.id.textViewSignUpLink);
        textViewSignUpLink.setText(Html.fromHtml(getString(R.string.dont_have_account)), TextView.BufferType.SPANNABLE);

        // Inicializa o serviço de autenticação com Retrofit
        authService = ApiClient.getClient().create(AuthService.class);

        // Ação para o botão de login
        buttonLogin.setOnClickListener(view -> loginUser());
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        LoginRequest request = new LoginRequest(email, password);
        authService.loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(LoginActivity.this, "Login bem-sucedido", Toast.LENGTH_SHORT).show();
                    // Armazene o token, se necessário, e navegue para a MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
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
}
