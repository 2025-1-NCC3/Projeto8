package br.fecap.pi.ubersafestart;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

// Importações da API e Modelos
import br.fecap.pi.ubersafestart.api.ApiClient;
import br.fecap.pi.ubersafestart.api.AuthService;
import br.fecap.pi.ubersafestart.model.ApiResponse;
// Certifique-se que está a usar a versão CORRIGIDA de ProfileResponse (ID: profile_response_fix)
import br.fecap.pi.ubersafestart.model.ProfileResponse;
import br.fecap.pi.ubersafestart.model.SafeScoreResponse;
// Se for implementar alteração de gênero:
import br.fecap.pi.ubersafestart.model.GenderUpdateRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    // Constantes para SharedPreferences
    private static final String USER_LOGIN_PREFS = "userPrefs";
    private static final String USER_LOCAL_PREFERENCES = "UserPreferences";
    private static final String KEY_GENDER = "gender"; // Chave consistente com o campo/JSON
    private static final String KEY_SAME_GENDER_PAIRING = "sameGenderPairingEnabled";

    // Componentes da UI
    private TextView textViewName, textViewEmail, textViewPhone, textViewAccountType;
    private TextView textViewRating, textViewSafeScore, textViewGender;
    private ProgressBar progressBarSafeScore;
    private TextView textViewPairingPreferencesLabel;
    private SwitchCompat switchSameGenderPairing;
    private Button buttonLogout, buttonEditProfile, buttonDeleteAccount;
    private LinearLayout navHome, navServices, navActivity, navAccount;

    // Variável de estado
    private String currentUserGenderNormalized = ""; // Armazena gênero NORMALIZADO (MAIÚSCULAS) para lógica interna

    // Serviços e Preferências
    private AuthService authService;
    private SharedPreferences sharedPreferences; // Para preferência local
    private SharedPreferences userLoginPrefs; // Para dados de login


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        initViews();
        authService = ApiClient.getClient().create(AuthService.class);
        sharedPreferences = getSharedPreferences(USER_LOCAL_PREFERENCES, Context.MODE_PRIVATE);
        userLoginPrefs = getSharedPreferences(USER_LOGIN_PREFS, MODE_PRIVATE);

        setupButtonListeners();
        setupNavigation();
        loadProfileData();
    }

    private void initViews() {
        textViewName = findViewById(R.id.textViewName);
        textViewEmail = findViewById(R.id.textViewEmail);
        textViewPhone = findViewById(R.id.textViewPhone);
        textViewAccountType = findViewById(R.id.textViewAccountType);
        textViewRating = findViewById(R.id.textViewRating);
        textViewSafeScore = findViewById(R.id.textViewSafeScore);
        progressBarSafeScore = findViewById(R.id.progressBarSafeScore);
        textViewGender = findViewById(R.id.textViewGender);
        textViewPairingPreferencesLabel = findViewById(R.id.textViewPairingPreferencesLabel);
        switchSameGenderPairing = findViewById(R.id.switchSameGenderPairing);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonEditProfile = findViewById(R.id.buttonEditProfile);
        buttonDeleteAccount = findViewById(R.id.buttonDeleteAccount);
        navHome = findViewById(R.id.navHome);
        navServices = findViewById(R.id.navServices);
        navActivity = findViewById(R.id.navActivity);
        navAccount = findViewById(R.id.navAccount);
    }

    private void setupButtonListeners() {
        buttonLogout.setOnClickListener(v -> logoutUser());
        buttonEditProfile.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Função de edição em desenvolvimento", Toast.LENGTH_SHORT).show();
            // Futuro: Permitir editar gênero chamando openGenderSelectionDialog();
        });
        buttonDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());

        if (switchSameGenderPairing != null) {
            switchSameGenderPairing.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.getVisibility() == View.VISIBLE) {
                    savePairingPreference(isChecked);
                    Toast.makeText(ProfileActivity.this,
                            isChecked ? "Preferência de parear com mesmo gênero ATIVADA" : "Preferência de parear com mesmo gênero DESATIVADA",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "SwitchCompat switchSameGenderPairing não encontrado!");
        }
    }

    private void setupNavigation() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.navHome) {
                navigateToHome();
            } else if (id == R.id.navServices) {
                Toast.makeText(ProfileActivity.this, "Opções em desenvolvimento", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.navActivity) {
                Toast.makeText(ProfileActivity.this, "Atividade em desenvolvimento", Toast.LENGTH_SHORT).show();
            }
            // Não há ação para navAccount pois já estamos aqui
        };
        if (navHome != null) navHome.setOnClickListener(listener);
        if (navServices != null) navServices.setOnClickListener(listener);
        if (navActivity != null) navActivity.setOnClickListener(listener);
        if (navAccount != null) navAccount.setOnClickListener(listener);
    }

    private void logoutUser() {
        Log.d(TAG, "Iniciando logout...");
        userLoginPrefs.edit().clear().apply();
        sharedPreferences.edit().remove(KEY_SAME_GENDER_PAIRING).apply();
        Log.d(TAG, "SharedPreferences limpas.");
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
        Log.d(TAG, "Redirecionado para LoginActivity.");
    }

    private void navigateToHome() {
        String userType = userLoginPrefs.getString("type", "");
        Intent intent;
        if ("driver".equalsIgnoreCase(userType)) {
            intent = new Intent(ProfileActivity.this, DriverHomeActivity.class);
        } else {
            intent = new Intent(ProfileActivity.this, HomeActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void showDeleteAccountConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Deletar Conta");
        builder.setMessage("Você tem certeza que deseja deletar sua conta? Esta ação não pode ser desfeita.");
        builder.setPositiveButton("Sim", (dialog, which) -> deleteUserAccount());
        builder.setNegativeButton("Não", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void deleteUserAccount() {
        String token = userLoginPrefs.getString("token", null);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Erro: Token de autenticação não encontrado.", Toast.LENGTH_SHORT).show();
            logoutUser();
            return;
        }
        sharedPreferences.edit().remove(KEY_SAME_GENDER_PAIRING).apply();
        Toast.makeText(this, "Processando solicitação...", Toast.LENGTH_SHORT).show();
        String bearerToken = token.startsWith("Bearer ") ? token : "Bearer " + token;

        authService.deleteUser(bearerToken).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ProfileActivity.this, "Conta deletada com sucesso", Toast.LENGTH_SHORT).show();
                    userLoginPrefs.edit().clear().apply();
                    Intent intent = new Intent(ProfileActivity.this, SignUpActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    finish();
                } else {
                    String errorMsg = "Falha ao deletar conta";
                    // ... (lógica de tratamento de erro) ...
                    Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Erro de conexão ao tentar deletar conta: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadProfileData() {
        Log.d(TAG, "Iniciando loadProfileData...");
        String token = userLoginPrefs.getString("token", null);

        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token não encontrado. Forçando logout.");
            Toast.makeText(this, "Sessão inválida.", Toast.LENGTH_LONG).show();
            logoutUser();
            return;
        }

        // Carrega dados locais primeiro
        Log.d(TAG, "Carregando dados iniciais das SharedPreferences (userPrefs)...");
        textViewName.setText(userLoginPrefs.getString("username", "Carregando..."));
        textViewEmail.setText(userLoginPrefs.getString("email", "Carregando..."));
        textViewPhone.setText(userLoginPrefs.getString("phone", "Carregando..."));
        String accountType = userLoginPrefs.getString("type", "");
        textViewAccountType.setText("driver".equalsIgnoreCase(accountType) ? "Motorista" : "Passageiro");
        int initialSafeScore = userLoginPrefs.getInt("safescore", 0);
        textViewSafeScore.setText(String.valueOf(initialSafeScore) + "/100");
        progressBarSafeScore.setProgress(initialSafeScore);
        textViewRating.setText("4.8"); // Estático

        // Carrega e normaliza o gênero das SharedPreferences
        String genderFromPrefs = userLoginPrefs.getString(KEY_GENDER, "");
        Log.d(TAG, "Gênero lido das SharedPreferences (userPrefs) ANTES da chamada API: '" + genderFromPrefs + "'");
        currentUserGenderNormalized = genderFromPrefs.toUpperCase(Locale.ROOT);
        Log.d(TAG, "Gênero Normalizado (MAIÚSCULAS) das prefs: '" + currentUserGenderNormalized + "'");

        // Exibe o gênero traduzido (ou 'Carregando...')
        if (!TextUtils.isEmpty(currentUserGenderNormalized)) {
            textViewGender.setText(translateGenderToPortuguese(genderFromPrefs)); // Usa tradução
        } else {
            textViewGender.setText("Carregando...");
        }

        setupPairingPreferenceSwitch();

        // Busca dados atualizados do servidor
        Log.d(TAG, "Iniciando chamada à API getProfile...");
        authService.getProfile("Bearer " + token).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                Log.d(TAG, "Resposta da API getProfile recebida. Código: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profile = response.body();
                    Log.d(TAG, "Perfil carregado com sucesso do servidor.");

                    textViewName.setText(profile.getUsername());
                    textViewEmail.setText(profile.getEmail());
                    textViewPhone.setText(profile.getPhone());
                    String serverAccountType = profile.getType();
                    textViewAccountType.setText("driver".equalsIgnoreCase(serverAccountType) ? "Motorista" : "Passageiro");

                    // Obtém e NORMALIZA o gênero do servidor
                    String serverGender = profile.getGender(); // Usa o getter correto
                    Log.d(TAG, "Gênero recebido do servidor (/api/profile): '" + serverGender + "'");
                    currentUserGenderNormalized = (serverGender != null) ? serverGender.toUpperCase(Locale.ROOT) : "";
                    Log.d(TAG, "Gênero Normalizado (MAIÚSCULAS) após API: '" + currentUserGenderNormalized + "'");

                    // Salva dados atualizados nas SharedPreferences
                    SharedPreferences.Editor editor = userLoginPrefs.edit();
                    editor.putString("username", profile.getUsername());
                    editor.putString("email", profile.getEmail());
                    editor.putString("phone", profile.getPhone());
                    editor.putString("type", profile.getType());
                    editor.putString(KEY_GENDER, (serverGender != null ? serverGender : "")); // Salva o valor correto ('male', 'female', 'other' ou "")
                    editor.putInt("safescore", profile.getSafescore());
                    editor.apply();
                    Log.d(TAG, "Dados atualizados salvos nas SharedPreferences (userPrefs). Gênero salvo: '" + (serverGender != null ? serverGender : "") + "'");

                    // Atualiza a UI do gênero (exibe traduzido)
                    if (!TextUtils.isEmpty(currentUserGenderNormalized)) {
                        // *** ALTERAÇÃO AQUI: Usa translateGenderToPortuguese ***
                        textViewGender.setText(translateGenderToPortuguese(serverGender));
                        Log.d(TAG, "Definindo texto do gênero (do servidor, traduzido): " + translateGenderToPortuguese(serverGender));
                    } else {
                        textViewGender.setText("Não informado");
                        Log.d(TAG, "Definindo texto do gênero como 'Não informado'");
                    }

                    textViewSafeScore.setText(String.valueOf(profile.getSafescore()) + "/100");
                    progressBarSafeScore.setProgress(profile.getSafescore());

                    setupPairingPreferenceSwitch();

                } else {
                    Log.e(TAG, "Erro ao carregar perfil da API. Código: " + response.code());
                    Toast.makeText(ProfileActivity.this, "Não foi possível atualizar dados do perfil (Erro: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    // Fallback para dados das prefs
                    String genderFromPrefsFallback = userLoginPrefs.getString(KEY_GENDER, "");
                    currentUserGenderNormalized = genderFromPrefsFallback.toUpperCase(Locale.ROOT);
                    Log.d(TAG, "Fallback: Usando gênero das SharedPreferences: '" + currentUserGenderNormalized + "'");
                    if (!TextUtils.isEmpty(currentUserGenderNormalized)) {
                        // *** ALTERAÇÃO AQUI: Usa translateGenderToPortuguese ***
                        textViewGender.setText(translateGenderToPortuguese(genderFromPrefsFallback));
                    } else {
                        textViewGender.setText("Não informado");
                    }
                    setupPairingPreferenceSwitch();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Log.e(TAG, "Falha na chamada API getProfile (rede): ", t);
                Toast.makeText(ProfileActivity.this, "Erro de rede ao carregar perfil.", Toast.LENGTH_SHORT).show();
                // Fallback para dados das prefs
                String genderFromPrefsFallback = userLoginPrefs.getString(KEY_GENDER, "");
                currentUserGenderNormalized = genderFromPrefsFallback.toUpperCase(Locale.ROOT);
                Log.d(TAG, "Fallback (onFailure): Usando gênero das SharedPreferences: '" + currentUserGenderNormalized + "'");
                if (!TextUtils.isEmpty(currentUserGenderNormalized)) {
                    // *** ALTERAÇÃO AQUI: Usa translateGenderToPortuguese ***
                    textViewGender.setText(translateGenderToPortuguese(genderFromPrefsFallback));
                } else {
                    textViewGender.setText("Não informado");
                }
                setupPairingPreferenceSwitch();
            }
        });
    }

    // *** MÉTODO MODIFICADO/RENOMEADO PARA TRADUZIR ***
    // Traduz o gênero recebido do backend ('male', 'female', 'other') para Português para exibição.
    private String translateGenderToPortuguese(String genderApiValue) {
        if (genderApiValue == null) {
            return "Não informado";
        }
        switch (genderApiValue.toLowerCase(Locale.ROOT)) { // Compara em minúsculas
            case "female":
                return "Feminino";
            case "male":
                return "Masculino";
            case "other":
                return "Outros";
            default:
                return "Não informado"; // Caso o valor seja inesperado ou vazio
        }
    }

    // Configura o switch de preferência de pareamento
    private void setupPairingPreferenceSwitch() {
        boolean isPairingEnabled = sharedPreferences.getBoolean(KEY_SAME_GENDER_PAIRING, false);
        Log.d(TAG, "Configurando Switch. Gênero Normalizado (para lógica): '" + currentUserGenderNormalized + "', Preferência salva: " + isPairingEnabled);

        if (textViewPairingPreferencesLabel == null || switchSameGenderPairing == null) {
            Log.e(TAG, "Erro: Label ou Switch de preferência não inicializados!");
            return;
        }

        // Lógica de visibilidade baseada no gênero NORMALIZADO (MAIÚSCULAS)
        if ("FEMALE".equals(currentUserGenderNormalized) || "OTHER".equals(currentUserGenderNormalized)) {
            Log.d(TAG, "Gênero é FEMALE ou OTHER. Habilitando switch.");
            textViewPairingPreferencesLabel.setVisibility(View.VISIBLE);
            switchSameGenderPairing.setVisibility(View.VISIBLE);
            switchSameGenderPairing.setOnCheckedChangeListener(null);
            switchSameGenderPairing.setChecked(isPairingEnabled);
            switchSameGenderPairing.setOnCheckedChangeListener((buttonView, isChecked) -> {
                savePairingPreference(isChecked);
                Toast.makeText(ProfileActivity.this,
                        isChecked ? "Preferência de parear com mesmo gênero ATIVADA" : "Preferência de parear com mesmo gênero DESATIVADA",
                        Toast.LENGTH_SHORT).show();
            });
        } else { // MALE ou vazio/inválido
            Log.d(TAG, "Gênero é MALE ou inválido/vazio. Desabilitando switch.");
            textViewPairingPreferencesLabel.setVisibility(View.GONE);
            switchSameGenderPairing.setVisibility(View.GONE);
            if (isPairingEnabled) {
                savePairingPreference(false);
            }
            switchSameGenderPairing.setOnCheckedChangeListener(null);
            switchSameGenderPairing.setChecked(false);
            switchSameGenderPairing.setOnCheckedChangeListener((buttonView, isChecked) -> {
                savePairingPreference(isChecked);
            });
        }
    }

    // Salva a preferência localmente
    private void savePairingPreference(boolean isEnabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SAME_GENDER_PAIRING, isEnabled);
        editor.apply();
        Log.d(TAG, "Preferência de pareamento salva localmente (" + USER_LOCAL_PREFERENCES + "): " + isEnabled);
    }

    // --- Métodos Opcionais para Alterar Gênero ---
    /*
    private void openGenderSelectionDialog() {
        final String[] gendersApi = {"female", "male", "other"}; // Valores para enviar à API (minúsculas)
        final String[] gendersDisplay = {"Feminino", "Masculino", "Outros"}; // Valores para exibir no diálogo

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle("Alterar Gênero");

        int currentGenderIndex = -1;
        String currentGenderUpper = currentUserGenderNormalized;
        for (int i = 0; i < gendersApi.length; i++) {
            if (gendersApi[i].toUpperCase(Locale.ROOT).equals(currentGenderUpper)) {
                currentGenderIndex = i;
                break;
            }
        }

        builder.setSingleChoiceItems(gendersDisplay, currentGenderIndex, (dialog, which) -> {
            String selectedGenderApiValue = gendersApi[which];
            Log.d(TAG, "Novo gênero selecionado para API: " + selectedGenderApiValue);
            updateUserGenderOnServer(selectedGenderApiValue);
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void updateUserGenderOnServer(String newGenderLowercase) {
        String token = userLoginPrefs.getString("token", null);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Erro de autenticação.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Tentando atualizar gênero no servidor para: " + newGenderLowercase);
        GenderUpdateRequest request = new GenderUpdateRequest(newGenderLowercase);

        // Certifique-se que AuthService tem o método updateUserGender definido corretamente
        authService.updateUserGender("Bearer " + token, request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "Gênero atualizado com sucesso no servidor.");
                    Toast.makeText(ProfileActivity.this, "Gênero atualizado com sucesso!", Toast.LENGTH_SHORT).show();

                    // Atualiza UI e SharedPreferences localmente
                    currentUserGenderNormalized = newGenderLowercase.toUpperCase(Locale.ROOT);
                    textViewGender.setText(translateGenderToPortuguese(newGenderLowercase)); // Exibe traduzido
                    userLoginPrefs.edit().putString(KEY_GENDER, newGenderLowercase).apply(); // Salva minúsculas
                    Log.d(TAG, "Gênero atualizado localmente (Normalizado): " + currentUserGenderNormalized);

                    setupPairingPreferenceSwitch();
                } else {
                    String errorMsg = "Falha ao atualizar gênero";
                    // ... (lógica de tratamento de erro) ...
                    Log.e(TAG, "Erro API updateUserGender: " + errorMsg);
                    Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                 Log.e(TAG, "Falha na chamada API updateUserGender (rede): ", t);
                 Toast.makeText(ProfileActivity.this, "Erro de rede ao atualizar gênero.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    */

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
