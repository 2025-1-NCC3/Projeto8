package br.fecap.pi.ubersafestart;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

import br.fecap.pi.ubersafestart.api.ApiClient;
import br.fecap.pi.ubersafestart.api.AuthService;
import br.fecap.pi.ubersafestart.model.ApiResponse;
import br.fecap.pi.ubersafestart.model.ProfileResponse;
import br.fecap.pi.ubersafestart.model.SafeScoreResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    // Constantes para SharedPreferences
    private static final String USER_LOGIN_PREFS = "userPrefs";
    private static final String USER_LOCAL_PREFERENCES = "UserPreferences";
    private static final String KEY_GENDER = "gender"; // Chave consistente com o campo/JSON
    private static final String KEY_SAME_GENDER_PAIRING = "sameGenderPairingEnabled";
    public static final String KEY_FACE_REGISTERED_PROTOTYPE = "face_registered_prototype";

    // Componentes da UI
    private TextView textViewName, textViewEmail, textViewPhone, textViewAccountType;
    private TextView textViewRating, textViewSafeScore, textViewGender;
    private ProgressBar progressBarSafeScore;
    private TextView textViewPairingPreferencesLabel;
    private SwitchCompat switchSameGenderPairing;
    private Button buttonLogout, buttonEditProfile, buttonDeleteAccount;
    private LinearLayout navHome, navServices, navAchievements, navAccount;
    private ImageView iconHome, iconServices, iconAchievementsView, iconAccountView;
    private TextView textHome, textServices, textAchievementsView, textAccountView;

    // NOVO: Botão para acessar gravações
    private Button buttonMyRecordings;

    // Componentes de verificação facial
    private Button buttonRegisterFacePrototype;
    private TextView textViewFaceRegistrationStatusPrototype;
    private ActivityResultLauncher<Intent> faceEnrollmentLauncher;

    private final int[] navIconIds = {R.id.iconHome, R.id.iconServices, R.id.iconAchievements, R.id.iconAccount};
    private final int[] navTextIds = {R.id.textHome, R.id.textServices, R.id.textAchievements, R.id.textAccount};

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
        setupFaceEnrollmentLauncher(); // Configura o launcher para a nova activity
        setupNavigationListeners();
        updateBottomNavigationSelection(R.id.navAccount);

        loadProfileData();
        loadFaceRegistrationStatus();
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

        // NOVO: Encontra o botão de Minhas Gravações
        buttonMyRecordings = findViewById(R.id.buttonMyRecordings);

        // Componentes de verificação facial
        buttonRegisterFacePrototype = findViewById(R.id.buttonRegisterFacePrototype);
        textViewFaceRegistrationStatusPrototype = findViewById(R.id.textViewFaceRegistrationStatusPrototype);

        if (buttonRegisterFacePrototype == null || textViewFaceRegistrationStatusPrototype == null) {
            Log.e(TAG, "Botão ou TextView de status para registro facial não encontrados no layout activity_profile.xml.");
        }

        // Componentes da Navegação Inferior
        navHome = findViewById(R.id.navHome);
        navServices = findViewById(R.id.navServices);
        navAchievements = findViewById(R.id.navAchievements);
        navAccount = findViewById(R.id.navAccount);

        iconHome = findViewById(R.id.iconHome);
        textHome = findViewById(R.id.textHome);
        iconServices = findViewById(R.id.iconServices);
        textServices = findViewById(R.id.textServices);
        iconAchievementsView = findViewById(R.id.iconAchievements);
        textAchievementsView = findViewById(R.id.textAchievements);
        iconAccountView = findViewById(R.id.iconAccount);
        textAccountView = findViewById(R.id.textAccount);
    }

    private void setupFaceEnrollmentLauncher() {
        faceEnrollmentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Registro facial simulado foi bem-sucedido na FaceEnrollmentActivity
                        // SharedPreferences já foi atualizado pela FaceEnrollmentActivity
                        Log.d(TAG, "Retorno de FaceEnrollmentActivity: SUCESSO.");
                        Toast.makeText(this, "Configuração facial concluída!", Toast.LENGTH_SHORT).show();
                    } else if (result.getResultCode() == RESULT_CANCELED) {
                        // Usuário cancelou ou houve falha na FaceEnrollmentActivity
                        Log.d(TAG, "Retorno de FaceEnrollmentActivity: CANCELADO ou FALHA.");
                        Toast.makeText(this, "Configuração facial não concluída.", Toast.LENGTH_SHORT).show();
                    }
                    // Atualiza o status na UI da ProfileActivity em ambos os casos
                    loadFaceRegistrationStatus();
                }
        );
    }

    private void loadFaceRegistrationStatus() {
        boolean isRegistered = userLoginPrefs.getBoolean(KEY_FACE_REGISTERED_PROTOTYPE, false);
        if (textViewFaceRegistrationStatusPrototype != null) {
            if (isRegistered) {
                textViewFaceRegistrationStatusPrototype.setText("Status: Verificação facial configurada (Simulado)");
                textViewFaceRegistrationStatusPrototype.setTextColor(ContextCompat.getColor(this, R.color.uber_green));
            } else {
                textViewFaceRegistrationStatusPrototype.setText("Status: Verificação facial não configurada");
                textViewFaceRegistrationStatusPrototype.setTextColor(ContextCompat.getColor(this, R.color.gray_medium));
            }
        }
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
                if (buttonView.getVisibility() == View.VISIBLE && buttonView.isPressed()) {
                    savePairingPreference(isChecked);
                    Toast.makeText(ProfileActivity.this,
                            isChecked ? "Preferência de parear com mesmo gênero ATIVADA" : "Preferência de parear com mesmo gênero DESATIVADA",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.e(TAG, "SwitchCompat switchSameGenderPairing não encontrado!");
        }

        if (buttonMyRecordings != null) {
            buttonMyRecordings.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, MyRecordingsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        } else {
            Log.e(TAG, "Button buttonMyRecordings não encontrado!");
        }

        // NOVO: Listener do botão de registro facial
        if (buttonRegisterFacePrototype != null) {
            buttonRegisterFacePrototype.setOnClickListener(v -> {
                Log.d(TAG, "Botão 'Configurar Verificação Facial' clicado. Abrindo FaceEnrollmentActivity.");
                Intent intent = new Intent(ProfileActivity.this, FaceEnrollmentActivity.class);
                faceEnrollmentLauncher.launch(intent); // Lança a activity esperando um resultado
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
    }

    private void setupNavigationListeners() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.navHome) {
                navigateToHome();
            } else if (id == R.id.navServices) {
                Intent intent = new Intent(ProfileActivity.this, TipsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } else if (id == R.id.navAchievements) {
                Intent intent = new Intent(ProfileActivity.this, AchievementsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            } else if (id == R.id.navAccount) {
                Toast.makeText(ProfileActivity.this, "Você já está na tela de Conta", Toast.LENGTH_SHORT).show();
                updateBottomNavigationSelection(R.id.navAccount);
            }
        };

        if (navHome != null) navHome.setOnClickListener(listener);
        if (navServices != null) navServices.setOnClickListener(listener);
        if (navAchievements != null) navAchievements.setOnClickListener(listener);
        if (navAccount != null) navAccount.setOnClickListener(listener);
    }

    private void logoutUser() {
        Log.d(TAG, "Iniciando logout...");

        SharedPreferences completedPrefs = getSharedPreferences("CompletedAchievements", MODE_PRIVATE);
        completedPrefs.edit().clear().apply();

        getSharedPreferences("CompletedAchievements", MODE_PRIVATE).edit().clear().apply();

        userLoginPrefs.edit().clear().apply();
        sharedPreferences.edit().clear().apply();
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
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
        sharedPreferences.edit().clear().apply();
        Toast.makeText(this, "Processando solicitação...", Toast.LENGTH_SHORT).show();
        String bearerToken = token.startsWith("Bearer ") ? token : "Bearer " + token;

        authService.deleteUser(bearerToken).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (isDestroyed() || isFinishing()) return;
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
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        } else if(response.body() != null && response.body().getMessage() != null){
                            errorMsg += ": " + response.body().getMessage();
                        } else {
                            errorMsg += " (Código: " + response.code() + ")";
                        }
                    } catch (Exception e) { /* ignore */ }
                    Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (isDestroyed() || isFinishing()) return;
                Toast.makeText(ProfileActivity.this, "Erro de conexão ao tentar deletar conta: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Este método busca o SafeScore do servidor e atualiza a UI.
     * A principal alteração está aqui para usar getBestAvailableScore().
     */
    public void fetchSafeScore(String token) {
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token não encontrado. Não é possível buscar SafeScore.");
            // Opcionalmente, atualize a UI para refletir pontuação ausente ou estado de erro
            // if (textViewSafeScore != null) textViewSafeScore.setText("N/A");
            // if (progressBarSafeScore != null) progressBarSafeScore.setProgress(0);
            return;
        }

        String bearerToken = token.startsWith("Bearer ") ? token : "Bearer " + token; // Garante o prefixo "Bearer "
        authService.getSafeScore(bearerToken).enqueue(new Callback<SafeScoreResponse>() {
            @Override
            public void onResponse(Call<SafeScoreResponse> call, Response<SafeScoreResponse> response) {
                if (isDestroyed() || isFinishing()) return;

                if (response.isSuccessful() && response.body() != null) {
                    // ***** INÍCIO DA MODIFICAÇÃO *****
                    // Usar getBestAvailableScore() para maior robustez na obtenção do valor.
                    int safeScore = response.body().getBestAvailableScore();
                    // ***** FIM DA MODIFICAÇÃO *****

                    Log.d(TAG, "SafeScore (melhor disponível) carregado com sucesso do servidor: " + safeScore);

                    // Atualizar a UI
                    if (textViewSafeScore != null) {
                        textViewSafeScore.setText(String.format(Locale.getDefault(), "%d/100", safeScore));
                    }
                    if (progressBarSafeScore != null) {
                        // Garante que o progresso esteja entre 0-100 (ou o máximo da barra)
                        progressBarSafeScore.setProgress(Math.max(0, Math.min(safeScore, 100)));
                    }

                    // Salvar no SharedPreferences
                    userLoginPrefs.edit().putInt("safescore", safeScore).apply();
                    Log.d(TAG, "SafeScore salvo nas SharedPreferences: " + safeScore);

                } else {
                    // Log detalhado do erro
                    String errorDetails = "Erro ao carregar SafeScore da API. Código: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorDetails += ", Corpo do Erro: " + response.errorBody().string();
                        } catch (java.io.IOException e) {
                            errorDetails += ", Falha ao ler corpo do erro.";
                        }
                    } else if (response.body() == null && response.isSuccessful()){
                        errorDetails += ", Corpo da resposta é nulo apesar de sucesso.";
                    }
                    Log.e(TAG, errorDetails);
                    // Opcionalmente, atualize a UI para mostrar erro ou pontuação anterior
                    // int lastKnownScore = userLoginPrefs.getInt("safescore", 0);
                    // if (textViewSafeScore != null) textViewSafeScore.setText(String.format(Locale.getDefault(), "%d/100 (Erro ao atualizar)", lastKnownScore));
                    // if (progressBarSafeScore != null) progressBarSafeScore.setProgress(Math.max(0, Math.min(lastKnownScore, 100)));
                }
            }

            @Override
            public void onFailure(Call<SafeScoreResponse> call, Throwable t) {
                if (isDestroyed() || isFinishing()) return;
                Log.e(TAG, "Falha na chamada API getSafeScore (rede): ", t);
                // Opcionalmente, trate a falha de rede na UI
                // int lastKnownScore = userLoginPrefs.getInt("safescore", 0);
                // if (textViewSafeScore != null) textViewSafeScore.setText(String.format(Locale.getDefault(), "%d/100 (Falha de rede)", lastKnownScore));
                // if (progressBarSafeScore != null) progressBarSafeScore.setProgress(Math.max(0, Math.min(lastKnownScore, 100)));
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
        textViewSafeScore.setText(String.format(Locale.getDefault(), "%d/100", initialSafeScore));
        if (progressBarSafeScore != null) { // Adicionada verificação de nulidade para segurança
            progressBarSafeScore.setProgress(Math.max(0, Math.min(initialSafeScore, 100)));
        }
        textViewRating.setText(userLoginPrefs.getString("rating", "4.8")); // Rating pode vir de outra fonte/lógica

        // Busca o SafeScore mais recente da API
        fetchSafeScore(token);

        // Lógica para buscar outros dados do perfil (ex: gênero) via /api/profile
        String bearerToken = token.startsWith("Bearer ") ? token : "Bearer " + token;
        authService.getProfile(bearerToken).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (isDestroyed() || isFinishing()) return;

                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profile = response.body();
                    Log.d(TAG, "Dados do perfil (/api/profile) carregados: " + profile.toString());

                    // Atualiza nome, email, telefone, tipo de conta se necessário (pode já estar ok das prefs)
                    if (profile.getUsername() != null) textViewName.setText(profile.getUsername());
                    if (profile.getEmail() != null) textViewEmail.setText(profile.getEmail());
                    if (profile.getPhone() != null) textViewPhone.setText(profile.getPhone());
                    if (profile.getType() != null) textViewAccountType.setText("driver".equalsIgnoreCase(profile.getType()) ? "Motorista" : "Passageiro");

                    // Gênero e preferências de pareamento
                    currentUserGenderNormalized = (profile.getGender() != null) ? profile.getGender().toUpperCase(Locale.ROOT) : "";
                    if (textViewGender != null) {
                        textViewGender.setText(translateGenderToPortuguese(profile.getGender()));
                    }
                    setupPairingPreferenceSwitch();

                    // Salvar os dados atualizados do perfil nas SharedPreferences (userPrefs)
                    SharedPreferences.Editor editor = userLoginPrefs.edit();
                    if (profile.getUsername() != null) editor.putString("username", profile.getUsername());
                    if (profile.getEmail() != null) editor.putString("email", profile.getEmail());
                    if (profile.getPhone() != null) editor.putString("phone", profile.getPhone());
                    if (profile.getType() != null) editor.putString("type", profile.getType());
                    if (profile.getGender() != null) editor.putString(KEY_GENDER, profile.getGender());
                    // Não salvar SafeScore daqui, pois fetchSafeScore já cuida disso de forma mais direta.
                    editor.apply();

                } else {
                    Log.e(TAG, "Erro ao carregar dados do perfil (/api/profile). Código: " + response.code());
                    // Poderia tentar carregar gênero das SharedPreferences como fallback se necessário
                    currentUserGenderNormalized = userLoginPrefs.getString(KEY_GENDER, "").toUpperCase(Locale.ROOT);
                    if (textViewGender != null) {
                        textViewGender.setText(translateGenderToPortuguese(userLoginPrefs.getString(KEY_GENDER, null)));
                    }
                    setupPairingPreferenceSwitch();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                if (isDestroyed() || isFinishing()) return;
                Log.e(TAG, "Falha na chamada API getProfile (rede): ", t);
                Toast.makeText(ProfileActivity.this, "Erro de conexão ao carregar perfil.", Toast.LENGTH_SHORT).show();
                // Carregar dados de fallback das SharedPreferences
                currentUserGenderNormalized = userLoginPrefs.getString(KEY_GENDER, "").toUpperCase(Locale.ROOT);
                if (textViewGender != null) {
                    textViewGender.setText(translateGenderToPortuguese(userLoginPrefs.getString(KEY_GENDER, null)));
                }
                setupPairingPreferenceSwitch();
            }
        });
    }


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
            switchSameGenderPairing.setOnCheckedChangeListener(null); // Evitar trigger ao setar
            switchSameGenderPairing.setChecked(isPairingEnabled);
            switchSameGenderPairing.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) { // Apenas se a mudança for por interação do usuário
                    savePairingPreference(isChecked);
                    Toast.makeText(ProfileActivity.this,
                            isChecked ? "Preferência de parear com mesmo gênero ATIVADA" : "Preferência de parear com mesmo gênero DESATIVADA",
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else { // MALE ou vazio/inválido
            Log.d(TAG, "Gênero é MALE ou inválido/vazio. Desabilitando switch.");
            textViewPairingPreferencesLabel.setVisibility(View.GONE);
            switchSameGenderPairing.setVisibility(View.GONE);
            if (isPairingEnabled) { // Se estava ativo para um gênero que não permite, desativa
                savePairingPreference(false);
            }
            switchSameGenderPairing.setOnCheckedChangeListener(null); // Evitar trigger
            switchSameGenderPairing.setChecked(false);
            // Reatribui listener caso seja necessário no futuro, embora invisível
            switchSameGenderPairing.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isPressed()) savePairingPreference(isChecked);
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

    private void updateBottomNavigationSelection(int selectedItemId) {
        LinearLayout[] navItemsLayouts = {navHome, navServices, navAchievements, navAccount};
        ImageView[] navIconsViews = {iconHome, iconServices, iconAchievementsView, iconAccountView};
        TextView[] navTextsViews = {textHome, textServices, textAchievementsView, textAccountView};
        int activeColor = ContextCompat.getColor(this, R.color.white_fff);
        int inactiveColor = ContextCompat.getColor(this, R.color.gray_light);

        for (int i = 0; i < navItemsLayouts.length; i++) {
            LinearLayout itemLayout = navItemsLayouts[i];
            ImageView icon = navIconsViews[i];
            TextView text = navTextsViews[i];
            if (itemLayout == null || icon == null || text == null) continue;
            boolean isActive = (itemLayout.getId() == selectedItemId);
            icon.setImageTintList(ColorStateList.valueOf(isActive ? activeColor : inactiveColor));
            text.setTextColor(isActive ? activeColor : inactiveColor);
        }
    }

    @Override
    public void onBackPressed() {
        navigateToHome();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBottomNavigationSelection(R.id.navAccount);
        loadFaceRegistrationStatus(); // Atualiza o status ao voltar para a tela

        String token = userLoginPrefs.getString("token", "");
        if (!TextUtils.isEmpty(token)) { // Verificação mais robusta para token
            fetchSafeScore(token);
        } else {
            Log.w(TAG, "onResume: Token é nulo ou vazio, não buscando SafeScore.");
            // Pode ser necessário tratar logout se o token estiver consistentemente ausente
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpeza de listeners ou outros recursos, se necessário
    }
}