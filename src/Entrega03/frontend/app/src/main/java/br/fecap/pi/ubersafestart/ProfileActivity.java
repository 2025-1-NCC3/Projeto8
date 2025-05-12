package br.fecap.pi.ubersafestart;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList; // Importar ColorStateList
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
// import android.widget.CompoundButton; // Removido se não usado diretamente no listener do switch após a mudança
import android.widget.ImageView; // Importar ImageView
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat; // Importar ContextCompat

// Importações da API e Modelos
import br.fecap.pi.ubersafestart.api.ApiClient;
import br.fecap.pi.ubersafestart.api.AuthService;
import br.fecap.pi.ubersafestart.model.ApiResponse;
import br.fecap.pi.ubersafestart.model.ProfileResponse;
// import br.fecap.pi.ubersafestart.model.SafeScoreResponse; // Não usado diretamente
// import br.fecap.pi.ubersafestart.model.GenderUpdateRequest; // Não usado diretamente

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    // Constantes para SharedPreferences
    private static final String USER_LOGIN_PREFS = "userPrefs";
    private static final String USER_LOCAL_PREFERENCES = "UserPreferences";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_SAME_GENDER_PAIRING = "sameGenderPairingEnabled";

    // Componentes da UI
    private TextView textViewName, textViewEmail, textViewPhone, textViewAccountType;
    private TextView textViewRating, textViewSafeScore, textViewGender;
    private ProgressBar progressBarSafeScore;
    private TextView textViewPairingPreferencesLabel;
    private SwitchCompat switchSameGenderPairing;
    private Button buttonLogout, buttonEditProfile, buttonDeleteAccount;
    private LinearLayout navHome, navServices, navAchievements, navAccount;

    // MODIFICAÇÃO: Adicionar referências diretas aos Ícones e Textos da Barra de Navegação
    private ImageView iconHome, iconServices, iconAchievementsView, iconAccountView; // Renomeado iconAchievements e iconAccount para evitar conflito de nome com LinearLayouts
    private TextView textHome, textServices, textAchievementsView, textAccountView; // Renomeado textAchievements e textAccount

    // Array de IDs para facilitar a atualização da barra de navegação
    // Assumindo que os IDs no XML de ProfileActivity são os mesmos de activity_home.xml para a barra de navegação
    private final int[] navIconIds = {R.id.iconHome, R.id.iconServices, R.id.iconAchievements, R.id.iconAccount};
    private final int[] navTextIds = {R.id.textHome, R.id.textServices, R.id.textAchievements, R.id.textAccount};


    // Variável de estado
    private String currentUserGenderNormalized = "";

    // Serviços e Preferências
    private AuthService authService;
    private SharedPreferences sharedPreferences;
    private SharedPreferences userLoginPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // IMPORTANT: Certifique-se que R.layout.activity_profile inclui a barra de navegação
        // com os IDs corretos (iconHome, textHome, etc.)
        setContentView(R.layout.activity_profile);

        initViews();
        authService = ApiClient.getClient().create(AuthService.class);
        sharedPreferences = getSharedPreferences(USER_LOCAL_PREFERENCES, Context.MODE_PRIVATE);
        userLoginPrefs = getSharedPreferences(USER_LOGIN_PREFS, MODE_PRIVATE);

        setupButtonListeners();
        setupNavigationListeners(); // Renomeado de setupNavigation para setupNavigationListeners
        updateBottomNavigationSelection(R.id.navAccount); // DESTAQUE INICIAL para Conta
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

        // Bottom navigation LinearLayouts
        navHome = findViewById(R.id.navHome);
        navServices = findViewById(R.id.navServices);
        navAchievements = findViewById(R.id.navAchievements); // LinearLayout clicável
        navAccount = findViewById(R.id.navAccount);         // LinearLayout clicável

        // MODIFICAÇÃO: Inicializar Ícones e Textos da barra de navegação
        // Certifique-se que estes IDs existem no R.layout.activity_profile (ou no layout incluído)
        iconHome = findViewById(R.id.iconHome); //ImageView dentro do navHome
        textHome = findViewById(R.id.textHome); //TextView dentro do navHome

        iconServices = findViewById(R.id.iconServices);
        textServices = findViewById(R.id.textServices);

        iconAchievementsView = findViewById(R.id.iconAchievements); //ImageView dentro do navAchievements
        textAchievementsView = findViewById(R.id.textAchievements); //TextView dentro do navAchievements

        iconAccountView = findViewById(R.id.iconAccount); //ImageView dentro do navAccount
        textAccountView = findViewById(R.id.textAccount); //TextView dentro do navAccount

        // Verificação para garantir que os componentes da barra de navegação foram encontrados
        if (iconHome == null || textHome == null || iconServices == null || textServices == null ||
                iconAchievementsView == null || textAchievementsView == null || iconAccountView == null || textAccountView == null) {
            Log.e(TAG, "Um ou mais componentes da barra de navegação (ícones/textos) não foram encontrados no layout R.layout.activity_profile. Verifique os IDs.");
            // Considerar lançar uma exceção ou tratar de outra forma se for crítico
        }
        if (navHome == null || navServices == null || navAchievements == null || navAccount == null) {
            Log.e(TAG, "Um ou mais LinearLayouts da barra de navegação não foram encontrados. Verifique os IDs em R.layout.activity_profile.");
        }
    }

    private void setupButtonListeners() {
        buttonLogout.setOnClickListener(v -> logoutUser());
        buttonEditProfile.setOnClickListener(v -> {
            Toast.makeText(ProfileActivity.this, "Função de edição em desenvolvimento", Toast.LENGTH_SHORT).show();
        });
        buttonDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());

        if (switchSameGenderPairing != null) {
            switchSameGenderPairing.setOnCheckedChangeListener((buttonView, isChecked) -> {
                // Apenas salva e mostra Toast se o switch estiver visível e clicável
                if (switchSameGenderPairing.getVisibility() == View.VISIBLE && buttonView.isPressed()) {
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

    // Renomeado para setupNavigationListeners
    private void setupNavigationListeners() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            Log.d(TAG, "Item de navegação clicado: " + getResources().getResourceEntryName(id));
            // updateBottomNavigationSelection(id); // Atualiza a seleção visual - MOVIDO PARA DENTRO DOS IFs ou OnResume

            if (id == R.id.navHome) {
                navigateToHome(); // Animação será aplicada aqui
            } else if (id == R.id.navServices) {
                Toast.makeText(ProfileActivity.this, "Opções em desenvolvimento", Toast.LENGTH_SHORT).show();
                updateBottomNavigationSelection(R.id.navServices);
            } else if (id == R.id.navAchievements) {
                Intent intent = new Intent(ProfileActivity.this, AchievementsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                // AchievementsActivity irá gerenciar seu próprio estado de seleção
            } else if (id == R.id.navAccount) {
                // Já está na tela de Conta
                Toast.makeText(ProfileActivity.this, "Você já está na tela de Conta", Toast.LENGTH_SHORT).show();
                updateBottomNavigationSelection(R.id.navAccount); // Reafirma o destaque
            }
        };

        if (navHome != null) navHome.setOnClickListener(listener); else Log.e(TAG, "navHome nulo em Profile");
        if (navServices != null) navServices.setOnClickListener(listener); else Log.e(TAG, "navServices nulo em Profile");
        if (navAchievements != null) navAchievements.setOnClickListener(listener); else Log.e(TAG, "navAchievements nulo em Profile");
        if (navAccount != null) navAccount.setOnClickListener(listener); else Log.e(TAG, "navAccount nulo em Profile");
    }

    private void logoutUser() {
        Log.d(TAG, "Iniciando logout...");
        // Limpa SharedPreferences de login e preferências locais de pareamento
        userLoginPrefs.edit().clear().apply();
        sharedPreferences.edit().remove(KEY_SAME_GENDER_PAIRING).apply(); // Limpa preferência de pareamento

        Log.d(TAG, "SharedPreferences limpas.");
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); // Animação padrão ao deslogar
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
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP); // Adicionado CLEAR_TOP
        startActivity(intent);
        // MODIFICAÇÃO: Animação slide_in_left ao ir para Home
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    private void showDeleteAccountConfirmation() {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("Deletar Conta")
                .setMessage("Você tem certeza que deseja deletar sua conta? Esta ação não pode ser desfeita.")
                .setPositiveButton("Sim", (dialog, which) -> deleteUserAccount())
                .setNegativeButton("Não", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void deleteUserAccount() {
        String token = userLoginPrefs.getString("token", null);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Erro: Token de autenticação não encontrado.", Toast.LENGTH_SHORT).show();
            logoutUser(); // Força logout se não houver token
            return;
        }

        // Limpa preferência de pareamento local antes de tentar deletar a conta
        sharedPreferences.edit().remove(KEY_SAME_GENDER_PAIRING).apply();

        Toast.makeText(this, "Processando solicitação...", Toast.LENGTH_SHORT).show();
        String bearerToken = token.startsWith("Bearer ") ? token : "Bearer " + token;

        authService.deleteUser(bearerToken).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (isDestroyed() || isFinishing()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ProfileActivity.this, "Conta deletada com sucesso", Toast.LENGTH_SHORT).show();
                    userLoginPrefs.edit().clear().apply(); // Limpa todos os dados de login
                    // Navega para SignUpActivity ou LoginActivity
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

    private void loadProfileData() {
        Log.d(TAG, "Iniciando loadProfileData...");
        String token = userLoginPrefs.getString("token", null);

        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token não encontrado. Forçando logout.");
            Toast.makeText(this, "Sessão inválida. Por favor, faça login novamente.", Toast.LENGTH_LONG).show();
            logoutUser();
            return;
        }

        // Carrega dados locais primeiro como fallback ou para exibição rápida
        textViewName.setText(userLoginPrefs.getString("username", "Carregando..."));
        textViewEmail.setText(userLoginPrefs.getString("email", "Carregando..."));
        textViewPhone.setText(userLoginPrefs.getString("phone", "Carregando..."));
        String accountType = userLoginPrefs.getString("type", "");
        textViewAccountType.setText("driver".equalsIgnoreCase(accountType) ? "Motorista" : "Passageiro");
        int initialSafeScore = userLoginPrefs.getInt("safescore", 0); // Usa 'safescore'
        textViewSafeScore.setText(String.format(Locale.getDefault(), "%d/100", initialSafeScore));
        progressBarSafeScore.setProgress(initialSafeScore);
        textViewRating.setText(userLoginPrefs.getString("rating", "4.8")); // Rating pode vir das prefs ou ser estático

        String genderFromPrefs = userLoginPrefs.getString(KEY_GENDER, "");
        currentUserGenderNormalized = genderFromPrefs.toUpperCase(Locale.ROOT);
        textViewGender.setText(TextUtils.isEmpty(genderFromPrefs) ? "Não informado" : translateGenderToPortuguese(genderFromPrefs));
        setupPairingPreferenceSwitch(); // Configura o switch com base no gênero das prefs

        // Busca dados atualizados do servidor
        Log.d(TAG, "Iniciando chamada à API getProfile...");
        authService.getProfile("Bearer " + token).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (isDestroyed() || isFinishing()) return;
                Log.d(TAG, "Resposta da API getProfile recebida. Código: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profile = response.body();
                    Log.d(TAG, "Perfil carregado com sucesso do servidor: " + profile.toString());

                    // Atualiza UI
                    textViewName.setText(profile.getUsername());
                    textViewEmail.setText(profile.getEmail());
                    textViewPhone.setText(profile.getPhone());
                    String serverAccountType = profile.getType();
                    textViewAccountType.setText("driver".equalsIgnoreCase(serverAccountType) ? "Motorista" : "Passageiro");

                    String serverGender = profile.getGender();
                    Log.d(TAG, "Gênero recebido do servidor (/api/profile): '" + serverGender + "'");
                    currentUserGenderNormalized = (serverGender != null) ? serverGender.toUpperCase(Locale.ROOT) : "";

                    // Atualiza gênero na UI
                    textViewGender.setText(TextUtils.isEmpty(serverGender) ? "Não informado" : translateGenderToPortuguese(serverGender));

                    // Atualiza SafeScore
                    int serverSafeScore = profile.getSafescore();
                    textViewSafeScore.setText(String.format(Locale.getDefault(), "%d/100", serverSafeScore));
                    progressBarSafeScore.setProgress(serverSafeScore);
                    // textViewRating.setText(String.valueOf(profile.getRating())); // Se o rating vier da API

                    // Salva dados atualizados nas SharedPreferences
                    SharedPreferences.Editor editor = userLoginPrefs.edit();
                    editor.putString("username", profile.getUsername());
                    editor.putString("email", profile.getEmail());
                    editor.putString("phone", profile.getPhone());
                    editor.putString("type", profile.getType());
                    editor.putString(KEY_GENDER, (serverGender != null ? serverGender : "")); // Salva 'male', 'female', 'other' ou ""
                    editor.putInt("safescore", serverSafeScore); // Usa 'safescore' consistentemente
                    // editor.putString("rating", String.valueOf(profile.getRating())); // Se aplicável
                    editor.apply();
                    Log.d(TAG, "Dados atualizados salvos nas SharedPreferences (userPrefs). Gênero salvo: '" + (serverGender != null ? serverGender : "") + "'");

                    setupPairingPreferenceSwitch(); // Reconfigura o switch com o gênero atualizado

                } else {
                    Log.e(TAG, "Erro ao carregar perfil da API. Código: " + response.code() + ". Usando dados locais se disponíveis.");
                    Toast.makeText(ProfileActivity.this, "Não foi possível atualizar todos os dados (Erro: " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    // Mantém os dados das prefs já carregados e reconfigura o switch
                    setupPairingPreferenceSwitch();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                if (isDestroyed() || isFinishing()) return;
                Log.e(TAG, "Falha na chamada API getProfile (rede): ", t);
                Toast.makeText(ProfileActivity.this, "Erro de rede ao carregar perfil. Verifique sua conexão.", Toast.LENGTH_SHORT).show();
                // Mantém os dados das prefs já carregados e reconfigura o switch
                setupPairingPreferenceSwitch();
            }
        });
    }

    private String translateGenderToPortuguese(String genderApiValue) {
        if (genderApiValue == null || genderApiValue.trim().isEmpty()) {
            return "Não informado";
        }
        switch (genderApiValue.toLowerCase(Locale.ROOT)) {
            case "female":
                return "Feminino";
            case "male":
                return "Masculino";
            case "other":
                return "Outro"; // Singular para "Outro"
            default:
                return "Não informado"; // Ou "Prefiro não informar", dependendo da UX
        }
    }

    private void setupPairingPreferenceSwitch() {
        boolean isPairingEnabledInitially = sharedPreferences.getBoolean(KEY_SAME_GENDER_PAIRING, false);
        Log.d(TAG, "Configurando Switch. Gênero Normalizado (para lógica): '" + currentUserGenderNormalized + "', Preferência salva: " + isPairingEnabledInitially);

        if (textViewPairingPreferencesLabel == null || switchSameGenderPairing == null) {
            Log.e(TAG, "Erro: Label ou Switch de preferência não inicializados!");
            return;
        }

        // Lógica de visibilidade baseada no gênero NORMALIZADO (MAIÚSCULAS)
        // Visível para FEMALE ou OTHER. Oculto para MALE ou não informado/inválido.
        if ("FEMALE".equals(currentUserGenderNormalized) || "OTHER".equals(currentUserGenderNormalized)) {
            Log.d(TAG, "Gênero é FEMALE ou OTHER. Habilitando switch de pareamento.");
            textViewPairingPreferencesLabel.setVisibility(View.VISIBLE);
            switchSameGenderPairing.setVisibility(View.VISIBLE);
            // Define o estado do switch sem disparar o listener programaticamente
            switchSameGenderPairing.setChecked(isPairingEnabledInitially);
        } else { // MALE, vazio, ou inválido
            Log.d(TAG, "Gênero é MALE, não informado, ou inválido ('" + currentUserGenderNormalized + "'). Desabilitando e ocultando switch de pareamento.");
            textViewPairingPreferencesLabel.setVisibility(View.GONE);
            switchSameGenderPairing.setVisibility(View.GONE);
            // Se o switch estava ativo para um gênero que não permite, desativa a preferência
            if (isPairingEnabledInitially) {
                savePairingPreference(false); // Garante que a preferência seja desativada
            }
            switchSameGenderPairing.setChecked(false); // Garante que o switch esteja desmarcado
        }
    }

    private void savePairingPreference(boolean isEnabled) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_SAME_GENDER_PAIRING, isEnabled);
        editor.apply();
        Log.d(TAG, "Preferência de pareamento salva localmente (" + USER_LOCAL_PREFERENCES + "): " + isEnabled);
    }

    // MODIFICAÇÃO: Método para atualizar a seleção da barra de navegação (similar ao de HomeActivity)
    private void updateBottomNavigationSelection(int selectedItemId) {
        // Array dos LinearLayouts da navegação
        LinearLayout[] navItemsLayouts = {navHome, navServices, navAchievements, navAccount};
        // Array dos ImageViews (ícones) e TextViews (textos) correspondentes
        // Usando os nomes de variáveis corretos definidos em initViews()
        ImageView[] navIconsViews = {iconHome, iconServices, iconAchievementsView, iconAccountView};
        TextView[] navTextsViews = {textHome, textServices, textAchievementsView, textAccountView};


        int activeColor = ContextCompat.getColor(this, R.color.white_fff); // Cor branca para ativo
        int inactiveColor = ContextCompat.getColor(this, R.color.gray_light); // Cor cinza para inativo

        for (int i = 0; i < navItemsLayouts.length; i++) {
            LinearLayout itemLayout = navItemsLayouts[i];
            ImageView icon = navIconsViews[i]; // Usa o array de ImageViews
            TextView text = navTextsViews[i];   // Usa o array de TextViews

            // Verificação crucial: garante que os componentes visuais existam
            if (itemLayout == null) {
                Log.w(TAG, "LinearLayout de navegação na posição " + i + " é nulo.");
                continue;
            }
            if (icon == null) {
                Log.w(TAG, "ImageView (ícone) de navegação na posição " + i + " (ID esperado: " + (navIconIds.length > i ? getResources().getResourceEntryName(navIconIds[i]) : "N/A") + ") é nulo. Verifique o layout `activity_profile.xml`.");
                continue;
            }
            if (text == null) {
                Log.w(TAG, "TextView (texto) de navegação na posição " + i + " (ID esperado: " + (navTextIds.length > i ? getResources().getResourceEntryName(navTextIds[i]) : "N/A") + ") é nulo. Verifique o layout `activity_profile.xml`.");
                continue;
            }

            boolean isActive = (itemLayout.getId() == selectedItemId);

            icon.setImageTintList(ColorStateList.valueOf(isActive ? activeColor : inactiveColor));
            text.setTextColor(isActive ? activeColor : inactiveColor);
        }
    }

    @Override
    public void onBackPressed() {
        // MODIFICAÇÃO: Navegar para Home com animação de slide_in_left
        // Em vez de super.onBackPressed() que usaria a animação padrão de saida (geralmente fade ou slide_out_right)
        navigateToHome();
        // Não chame super.onBackPressed() se você está gerenciando a transição explicitamente
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Garante que o item "Conta" esteja selecionado ao retornar para esta tela
        updateBottomNavigationSelection(R.id.navAccount);
        // Recarrega os dados do perfil caso algo possa ter mudado (ex: se vier de uma tela de edição)
        // loadProfileData(); // Descomente se houver cenários onde os dados do perfil podem mudar enquanto esta tela não está no topo.
    }
}