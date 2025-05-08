package br.fecap.pi.ubersafestart;

import android.content.Intent;
import android.graphics.Color; // Import para Color
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper; // Import necessário
import android.util.Log;   // <<<--- IMPORT ADICIONADO AQUI
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import br.fecap.pi.ubersafestart.api.ApiClient;
import br.fecap.pi.ubersafestart.api.AuthService;
import br.fecap.pi.ubersafestart.model.ApiResponse;
import br.fecap.pi.ubersafestart.model.User; // Certifique-se que User.java está atualizado (só com genero String)

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    private EditText editTextFullName, editTextEmailSignUp, editTextPasswordSignUp, editTextConfirmPasswordSignUp, editTextPhoneSignUp;
    private Button buttonSignUp;
    private MaterialCardView cardViewPassenger, cardViewDriver;
    private AuthService authService;
    private String selectedAccountType = "passenger";

    private RadioGroup radioGroupGender;

    // Padrões de validação
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +       //Pelo menos um dígito
                    "(?=.*[a-zA-Z])" +    //Pelo menos uma letra
                    "(?=.*[!@#$%^&*])" + //Pelo menos um caractere especial
                    "(?=\\S+$)" +       //Sem espaços em branco
                    ".{8,}" +           //Pelo menos 8 caracteres
                    "$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9]{10,15}$"); // Aceita números de telefone de 10 a 15 dígitos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Inicializa Views
        initViews();

        // Inicializa API Service
        authService = ApiClient.getClient().create(AuthService.class);

        // Configura seleção de tipo de conta
        setupAccountTypeSelection();

        // Configura botão Voltar
        View buttonBack = findViewById(R.id.buttonBackSignUp);
        if (buttonBack != null) {
            buttonBack.setOnClickListener(v -> {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            });
        }

        // Configura link para Login
        TextView textViewLoginLink = findViewById(R.id.textViewLoginLink);
        textViewLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });

        // Configura botão de Cadastro
        buttonSignUp.setOnClickListener(v -> signUpUser());
    }

    // Método para inicializar as views
    private void initViews() {
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextEmailSignUp = findViewById(R.id.editTextEmailSignUp);
        editTextPasswordSignUp = findViewById(R.id.editTextPasswordSignUp);
        editTextConfirmPasswordSignUp = findViewById(R.id.editTextConfirmPasswordSignUp);
        editTextPhoneSignUp = findViewById(R.id.editTextPhoneSignUp);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        cardViewPassenger = findViewById(R.id.cardViewPassenger);
        cardViewDriver = findViewById(R.id.cardViewDriver);
        radioGroupGender = findViewById(R.id.radioGroupGender);
    }


    // Configura a lógica visual para seleção de tipo de conta (Passageiro/Motorista)
    private void setupAccountTypeSelection() {
        // Define passageiro como padrão visualmente e na lógica
        selectedAccountType = "passenger";
        // Define cores para indicar seleção (ajuste as cores conforme seu tema)
        // Certifique-se que R.color.primary_color e R.color.gray_color existem em colors.xml
        try {
            cardViewPassenger.setStrokeColor(getResources().getColor(R.color.primary_color, getTheme()));
            cardViewDriver.setStrokeColor(getResources().getColor(R.color.gray_color, getTheme()));
        } catch (Exception e) {
            Log.e("SignUpActivity", "Erro ao definir cores dos cards. Verifique colors.xml", e);
            // Fallback colors
            cardViewPassenger.setStrokeColor(Color.WHITE);
            cardViewDriver.setStrokeColor(Color.GRAY);
        }


        // Listener para card Passageiro
        cardViewPassenger.setOnClickListener(v -> {
            selectedAccountType = "passenger";
            try {
                cardViewPassenger.setStrokeColor(getResources().getColor(R.color.primary_color, getTheme()));
                cardViewDriver.setStrokeColor(getResources().getColor(R.color.gray_color, getTheme()));
            } catch (Exception e) {
                cardViewPassenger.setStrokeColor(Color.WHITE);
                cardViewDriver.setStrokeColor(Color.GRAY);
            }
        });

        // Listener para card Motorista
        cardViewDriver.setOnClickListener(v -> {
            selectedAccountType = "driver";
            try {
                cardViewDriver.setStrokeColor(getResources().getColor(R.color.primary_color, getTheme()));
                cardViewPassenger.setStrokeColor(getResources().getColor(R.color.gray_color, getTheme()));
            } catch (Exception e) {
                cardViewDriver.setStrokeColor(Color.WHITE);
                cardViewPassenger.setStrokeColor(Color.GRAY);
            }
        });
    }

    // Valida a senha
    private boolean isPasswordValid(String password) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    // Valida o telefone
    private boolean isPhoneValid(String phone) {
        if (phone == null) return false;
        return PHONE_PATTERN.matcher(phone).matches();
    }

    // Processa o cadastro do usuário
    private void signUpUser() {
        // Obtém os valores dos campos
        String fullName = editTextFullName.getText().toString().trim();
        String email = editTextEmailSignUp.getText().toString().trim();
        String password = editTextPasswordSignUp.getText().toString().trim();
        String confirmPassword = editTextConfirmPasswordSignUp.getText().toString().trim();
        String phone = editTextPhoneSignUp.getText().toString().trim();

        // Obtém o gênero selecionado e converte para o formato esperado pelo backend (minúsculas)
        String selectedGender = "";
        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();

        // Usar valores em Minúsculas para enviar ao backend
        if (selectedGenderId == R.id.radioButtonMasculino) {
            selectedGender = "male";
        } else if (selectedGenderId == R.id.radioButtonFeminino) {
            selectedGender = "female";
        } else if (selectedGenderId == R.id.radioButtonOutros) {
            selectedGender = "other";
        } else {
            // Se nenhum gênero foi selecionado
            Toast.makeText(this, "Por favor, selecione seu gênero", Toast.LENGTH_SHORT).show();
            return; // Interrompe o processo
        }

        // Validações básicas dos campos
        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor, insira um email válido", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isPasswordValid(password)) {
            Toast.makeText(this, "A senha deve ter no mínimo 8 caracteres, incluindo letras, números e caracteres especiais (!@#$%^&*)", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isPhoneValid(phone)) {
            Toast.makeText(this, "Por favor, insira um número de telefone válido (10-15 dígitos)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Cria o objeto User com os dados corretos (gênero em minúsculas)
        User user = new User(fullName, email, password, selectedAccountType, phone, selectedGender);

        // Exibe um Toast indicando o início do processo
        Toast.makeText(this, "Registrando...", Toast.LENGTH_SHORT).show();

        // Chama a API para registrar o usuário
        authService.registerUser(user).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                try {
                    // Verifica se a resposta da API foi bem-sucedida
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(SignUpActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        // Redireciona para a tela de Login após um pequeno atraso
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                            finish(); // Fecha a tela de cadastro
                        }, 1000); // Atraso de 1 segundo
                    } else {
                        // Trata falha no cadastro (resposta da API não foi sucesso)
                        String errorMsg = "Falha no cadastro";
                        if (response.body() != null && response.body().getMessage() != null && !response.body().getMessage().isEmpty()) {
                            errorMsg = response.body().getMessage(); // Usa mensagem da API se disponível
                        } else if (response.errorBody() != null) {
                            try {
                                // Tenta ler mensagem do corpo de erro (pode ser HTML ou JSON)
                                String errorBodyStr = response.errorBody().string();
                                errorMsg = "Erro " + response.code() + ". Tente novamente."; // Mensagem genérica
                                Log.e("SignUpError", "Error Body: " + errorBodyStr); // Log para depuração
                            } catch (Exception e) {
                                errorMsg = "Falha ao processar resposta do servidor (Erro " + response.code() + ").";
                            }
                        } else {
                            errorMsg = "Erro desconhecido no cadastro (Código: " + response.code() + ").";
                        }
                        Toast.makeText(SignUpActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    // Trata exceções inesperadas durante o processamento da resposta
                    Toast.makeText(SignUpActivity.this, "Erro inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("SignUpException", "Erro ao processar resposta: ", e); // Log do erro
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Trata falha de rede ou conexão
                Toast.makeText(SignUpActivity.this, "Erro de rede: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("SignUpFailure", "Erro de rede: ", t); // Log do erro
            }
        });
    }

    // Lida com o botão Voltar do Android
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Animação de transição ao voltar
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
