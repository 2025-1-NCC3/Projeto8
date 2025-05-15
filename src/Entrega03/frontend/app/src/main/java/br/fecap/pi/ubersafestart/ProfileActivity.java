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
// Removido: import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull; // Mantido se usado em outros lugares
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
// Removido: import androidx.camera.core.* e PreviewView, pois a câmera agora está em FaceEnrollmentActivity
import androidx.core.content.ContextCompat; // Mantido

// Removido: import com.google.common.util.concurrent.ListenableFuture;
// Removido: import com.google.mlkit.vision.*
// Removido: import java.util.concurrent.ExecutionException;
// Removido: import java.util.concurrent.ExecutorService;
// Removido: import java.util.concurrent.Executors;
import java.util.Locale;


import br.fecap.pi.ubersafestart.api.ApiClient;
import br.fecap.pi.ubersafestart.api.AuthService;
import br.fecap.pi.ubersafestart.model.ApiResponse;
import br.fecap.pi.ubersafestart.model.ProfileResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final String USER_LOGIN_PREFS = "userPrefs";
    private static final String USER_LOCAL_PREFERENCES = "UserPreferences";
    private static final String KEY_GENDER = "gender";
    private static final String KEY_SAME_GENDER_PAIRING = "sameGenderPairingEnabled";
    public static final String KEY_FACE_REGISTERED_PROTOTYPE = "face_registered_prototype";

    private TextView textViewName, textViewEmail, textViewPhone, textViewAccountType;
    private TextView textViewRating, textViewSafeScore, textViewGender;
    private ProgressBar progressBarSafeScore;
    private TextView textViewPairingPreferencesLabel;
    private SwitchCompat switchSameGenderPairing;
    private Button buttonLogout, buttonEditProfile, buttonDeleteAccount;
    private LinearLayout navHome, navServices, navAchievements, navAccount;
    private ImageView iconHome, iconServices, iconAchievementsView, iconAccountView;
    private TextView textHome, textServices, textAchievementsView, textAccountView;

    private final int[] navIconIds = {R.id.iconHome, R.id.iconServices, R.id.iconAchievements, R.id.iconAccount};
    private final int[] navTextIds = {R.id.textHome, R.id.textServices, R.id.textAchievements, R.id.textAccount};

    private String currentUserGenderNormalized = "";
    private AuthService authService;
    private SharedPreferences sharedPreferences;
    private SharedPreferences userLoginPrefs;

    // --- Variáveis para Detecção Facial (agora apenas para lançar a nova activity) ---
    private Button buttonRegisterFacePrototype;
    private TextView textViewFaceRegistrationStatusPrototype;
    // Removido: PreviewView, CameraProvider, FaceDetector, Executor, etc.
    // --- Fim das variáveis de Detecção Facial ---

    // ActivityResultLauncher para a FaceEnrollmentActivity
    private ActivityResultLauncher<Intent> faceEnrollmentLauncher;


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
        // Views existentes
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

        // Views para detecção facial (botão e status)
        buttonRegisterFacePrototype = findViewById(R.id.buttonRegisterFacePrototype);
        textViewFaceRegistrationStatusPrototype = findViewById(R.id.textViewFaceRegistrationStatusPrototype);

        if (buttonRegisterFacePrototype == null || textViewFaceRegistrationStatusPrototype == null) {
            Log.e(TAG, "Botão ou TextView de status para registro facial não encontrados no layout activity_profile.xml.");
        }
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
        if (buttonLogout != null) buttonLogout.setOnClickListener(v -> logoutUser());
        if (buttonEditProfile != null) buttonEditProfile.setOnClickListener(v ->
                Toast.makeText(ProfileActivity.this, "Função de edição em desenvolvimento", Toast.LENGTH_SHORT).show()
        );
        if (buttonDeleteAccount != null) buttonDeleteAccount.setOnClickListener(v -> showDeleteAccountConfirmation());

        if (switchSameGenderPairing != null) {
            switchSameGenderPairing.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (switchSameGenderPairing.getVisibility() == View.VISIBLE && buttonView.isPressed()) {
                    savePairingPreference(isChecked);
                    Toast.makeText(ProfileActivity.this,
                            isChecked ? "Preferência de parear com mesmo gênero ATIVADA" : "Preferência de parear com mesmo gênero DESATIVADA",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }

        // MODIFICADO: Listener do botão de registro facial
        if (buttonRegisterFacePrototype != null) {
            buttonRegisterFacePrototype.setOnClickListener(v -> {
                Log.d(TAG, "Botão 'Configurar Verificação Facial' clicado. Abrindo FaceEnrollmentActivity.");
                Intent intent = new Intent(ProfileActivity.this, FaceEnrollmentActivity.class);
                faceEnrollmentLauncher.launch(intent); // Lança a activity esperando um resultado
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
    }


    // --- Métodos de câmera e detecção facial foram MOVIDOS para FaceEnrollmentActivity ---
    // onRequestPermissionsResult também não é mais necessário aqui para a câmera,
    // pois FaceEnrollmentActivity cuidará disso.

    private void setupNavigationListeners() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            if (id == R.id.navHome) {
                navigateToHome();
            } else if (id == R.id.navServices) {
                Toast.makeText(ProfileActivity.this, "Opções em desenvolvimento", Toast.LENGTH_SHORT).show();
                updateBottomNavigationSelection(R.id.navServices);
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
        userLoginPrefs.edit().clear().apply();
        sharedPreferences.edit().clear().apply();

        Log.d(TAG, "SharedPreferences limpas.");
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
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
            logoutUser();
            return;
        }

        Toast.makeText(this, "Processando solicitação...", Toast.LENGTH_SHORT).show();
        String bearerToken = token.startsWith("Bearer ") ? token : "Bearer " + token;

        authService.deleteUser(bearerToken).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (isDestroyed() || isFinishing()) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ProfileActivity.this, "Conta deletada com sucesso", Toast.LENGTH_SHORT).show();
                    logoutUser();
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
        String token = userLoginPrefs.getString("token", null);
        if (token == null || token.isEmpty()) {
            logoutUser();
            return;
        }

        if(textViewName!=null) textViewName.setText(userLoginPrefs.getString("username", "Carregando..."));
        if(textViewEmail!=null) textViewEmail.setText(userLoginPrefs.getString("email", "Carregando..."));
        if(textViewPhone!=null) textViewPhone.setText(userLoginPrefs.getString("phone", "Carregando..."));
        String accountType = userLoginPrefs.getString("type", "");
        if(textViewAccountType!=null) textViewAccountType.setText("driver".equalsIgnoreCase(accountType) ? "Motorista" : "Passageiro");
        int initialSafeScore = userLoginPrefs.getInt("safescore", 0);
        if(textViewSafeScore!=null) textViewSafeScore.setText(String.format(Locale.getDefault(), "%d/100", initialSafeScore));
        if(progressBarSafeScore!=null) progressBarSafeScore.setProgress(initialSafeScore);
        if(textViewRating!=null) textViewRating.setText(userLoginPrefs.getString("rating", "4.8"));

        String genderFromPrefs = userLoginPrefs.getString(KEY_GENDER, "");
        currentUserGenderNormalized = genderFromPrefs.toUpperCase(Locale.ROOT);
        if(textViewGender!=null) textViewGender.setText(TextUtils.isEmpty(genderFromPrefs) ? "Não informado" : translateGenderToPortuguese(genderFromPrefs));
        setupPairingPreferenceSwitch();

        authService.getProfile("Bearer " + token).enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (isDestroyed() || isFinishing()) return;
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profile = response.body();
                    if(textViewName!=null) textViewName.setText(profile.getUsername());
                    if(textViewEmail!=null) textViewEmail.setText(profile.getEmail());
                    if(textViewPhone!=null) textViewPhone.setText(profile.getPhone());
                    String serverAccountType = profile.getType();
                    if(textViewAccountType!=null) textViewAccountType.setText("driver".equalsIgnoreCase(serverAccountType) ? "Motorista" : "Passageiro");

                    String serverGender = profile.getGender();
                    currentUserGenderNormalized = (serverGender != null) ? serverGender.toUpperCase(Locale.ROOT) : "";
                    if(textViewGender!=null) textViewGender.setText(TextUtils.isEmpty(serverGender) ? "Não informado" : translateGenderToPortuguese(serverGender));

                    int serverSafeScore = profile.getSafescore();
                    if(textViewSafeScore!=null) textViewSafeScore.setText(String.format(Locale.getDefault(), "%d/100", serverSafeScore));
                    if(progressBarSafeScore!=null) progressBarSafeScore.setProgress(serverSafeScore);

                    SharedPreferences.Editor editor = userLoginPrefs.edit();
                    editor.putString("username", profile.getUsername());
                    editor.putString("email", profile.getEmail());
                    editor.putString("phone", profile.getPhone());
                    editor.putString("type", profile.getType());
                    editor.putString(KEY_GENDER, (serverGender != null ? serverGender : ""));
                    editor.putInt("safescore", serverSafeScore);
                    editor.apply();
                    setupPairingPreferenceSwitch();
                } else {
                    Log.e(TAG, "Erro ao carregar perfil da API. Código: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                if (isDestroyed() || isFinishing()) return;
                Log.e(TAG, "Falha na chamada API getProfile: ", t);
            }
        });
    }

    private String translateGenderToPortuguese(String genderApiValue) {
        if (genderApiValue == null || genderApiValue.trim().isEmpty()) return "Não informado";
        switch (genderApiValue.toLowerCase(Locale.ROOT)) {
            case "female": return "Feminino";
            case "male": return "Masculino";
            case "other": return "Outro";
            default: return "Não informado";
        }
    }

    private void setupPairingPreferenceSwitch() {
        boolean isPairingEnabledInitially = sharedPreferences.getBoolean(KEY_SAME_GENDER_PAIRING, false);
        if (textViewPairingPreferencesLabel == null || switchSameGenderPairing == null) return;

        if ("FEMALE".equals(currentUserGenderNormalized) || "OTHER".equals(currentUserGenderNormalized)) {
            textViewPairingPreferencesLabel.setVisibility(View.VISIBLE);
            switchSameGenderPairing.setVisibility(View.VISIBLE);
            switchSameGenderPairing.setChecked(isPairingEnabledInitially);
        } else {
            textViewPairingPreferencesLabel.setVisibility(View.GONE);
            switchSameGenderPairing.setVisibility(View.GONE);
            if (isPairingEnabledInitially) savePairingPreference(false);
            switchSameGenderPairing.setChecked(false);
        }
    }

    private void savePairingPreference(boolean isEnabled) {
        sharedPreferences.edit().putBoolean(KEY_SAME_GENDER_PAIRING, isEnabled).apply();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Não há mais câmera ou detector para fechar aqui
    }
}
