package br.fecap.pi.ubersafestart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.fecap.pi.ubersafestart.adapter.AchievementsAdapter;
import br.fecap.pi.ubersafestart.api.ApiClient;
import br.fecap.pi.ubersafestart.api.AuthService;
import br.fecap.pi.ubersafestart.model.Achievement;
import br.fecap.pi.ubersafestart.model.AchievementsResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AchievementsActivity extends AppCompatActivity {

    private static final String TAG = "AchievementsActivity";
    private static final String USER_LOGIN_PREFS = "userPrefs";

    // UI Components
    private TextView textViewUserLevel;
    private TextView textViewTotalPoints;
    private TextView textViewPointsToNextLevel;
    private ProgressBar progressBarNextLevel;
    private RecyclerView recyclerViewAchievements;
    private LinearLayout navHome, navServices, navAchievements, navAccount;

    // Referências diretas aos Ícones e Textos da Barra de Navegação
    private ImageView iconHome, iconServices, iconAchievements, iconAccount;
    private TextView textHome, textServices, textAchievements, textAccount;

    // Map para facilitar a atualização da barra de navegação
    private Map<Integer, View[]> navItemsMap;

    // Data
    private List<Achievement> achievementsList = new ArrayList<>();
    private AchievementsAdapter adapter;
    private AuthService authService;
    private int totalPoints = 0;
    private int userLevel = 1;

    private int loadAttempts = 0;
    private static final int MAX_LOAD_ATTEMPTS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        // Initialize views
        initViews();

        // Initialize API service
        authService = ApiClient.getClient().create(AuthService.class);

        // Setup RecyclerView
        recyclerViewAchievements.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AchievementsAdapter(this, achievementsList);
        recyclerViewAchievements.setAdapter(adapter);

        // Setup navigation
        setupNavigationListeners(); // CORREÇÃO: Garantir que os listeners sejam configurados
        updateBottomNavigationSelection(R.id.navAchievements);

        // Load achievements data
        loadAchievements();
    }

    private void initViews() {
        textViewUserLevel = findViewById(R.id.textViewUserLevel);
        textViewTotalPoints = findViewById(R.id.textViewTotalPoints);
        textViewPointsToNextLevel = findViewById(R.id.textViewPointsToNextLevel);
        progressBarNextLevel = findViewById(R.id.progressBarNextLevel);
        recyclerViewAchievements = findViewById(R.id.recyclerViewAchievements);

        // Bottom navigation LinearLayouts
        navHome = findViewById(R.id.navHome);
        navServices = findViewById(R.id.navServices);
        navAchievements = findViewById(R.id.navAchievements);
        navAccount = findViewById(R.id.navAccount);

        // Ícones e Textos da Barra de Navegação
        iconHome = findViewById(R.id.iconHome);
        textHome = findViewById(R.id.textHome);
        iconServices = findViewById(R.id.iconServices);
        textServices = findViewById(R.id.textServices);
        iconAchievements = findViewById(R.id.iconAchievements);
        textAchievements = findViewById(R.id.textAchievements);
        iconAccount = findViewById(R.id.iconAccount);
        textAccount = findViewById(R.id.textAccount);

        // Preencher o map para a barra de navegação
        navItemsMap = new HashMap<>();
        navItemsMap.put(R.id.navHome, new View[]{iconHome, textHome});
        navItemsMap.put(R.id.navServices, new View[]{iconServices, textServices});
        navItemsMap.put(R.id.navAchievements, new View[]{iconAchievements, textAchievements});
        navItemsMap.put(R.id.navAccount, new View[]{iconAccount, textAccount});
    }

    private void setupNavigationListeners() {
        navHome.setOnClickListener(v -> {
            if (!this.getClass().equals(HomeActivity.class)) {
                navigateToHome();
            }
        });

        navServices.setOnClickListener(v -> {
            navigateToServices();
        });

        navAchievements.setOnClickListener(v -> {
            Log.d(TAG, "Já está na tela de Conquistas.");
            updateBottomNavigationSelection(R.id.navAchievements); // Garante que está selecionado
        });

        navAccount.setOnClickListener(v -> {
            navigateToAccount();
        });
    }

    private void navigateToHome() {
        SharedPreferences prefs = getSharedPreferences(USER_LOGIN_PREFS, MODE_PRIVATE);
        String userType = prefs.getString("type", "");
        Intent intent;
        if ("driver".equalsIgnoreCase(userType)) {
            intent = new Intent(AchievementsActivity.this, DriverHomeActivity.class);
        } else {
            intent = new Intent(AchievementsActivity.this, HomeActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish(); // Finaliza a AchievementsActivity para não empilhar
    }

    private void navigateToServices() {
        Intent intent = new Intent(AchievementsActivity.this, TipsActivity.class);
        startActivity(intent);
        finish(); // Opcional: finalize se não quiser empilhar
        updateBottomNavigationSelection(R.id.navServices); // Atualiza a seleção visual
    }

    private void navigateToAccount() {
        Intent intent = new Intent(AchievementsActivity.this, ProfileActivity.class);
        startActivity(intent);
        finish(); // Opcional: finalize se não quiser empilhar
        updateBottomNavigationSelection(R.id.navAccount); // Atualiza a seleção visual
    }

    private void updateBottomNavigationSelection(int selectedItemId) {
        int activeColor = ContextCompat.getColor(this, R.color.white_fff); // Ou sua cor ativa, ex: uber_blue
        int inactiveColor = ContextCompat.getColor(this, R.color.gray_light);

        for (Map.Entry<Integer, View[]> entry : navItemsMap.entrySet()) {
            Integer navId = entry.getKey();
            ImageView icon = (ImageView) entry.getValue()[0];
            TextView text = (TextView) entry.getValue()[1];

            if (navId == selectedItemId) {
                icon.setImageTintList(ColorStateList.valueOf(activeColor));
                text.setTextColor(activeColor);
            } else {
                icon.setImageTintList(ColorStateList.valueOf(inactiveColor));
                text.setTextColor(inactiveColor);
            }
        }
    }

    private void loadAchievements() {
        SharedPreferences prefs = getSharedPreferences(USER_LOGIN_PREFS, MODE_PRIVATE);
        String token = prefs.getString("token", "");

        if (token.isEmpty()) {
            Toast.makeText(this, "Sessão inválida. Faça login novamente.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        String authHeader = "Bearer " + token;
        Log.d(TAG, "Carregando conquistas com token: " + authHeader);

        loadAttempts++;
        Log.d(TAG, "Tentativa #" + loadAttempts + " de carregar conquistas");

        authService.getUserAchievements(authHeader).enqueue(new Callback<AchievementsResponse>() {
            @Override
            public void onResponse(Call<AchievementsResponse> call, Response<AchievementsResponse> response) {
                if (isDestroyed() || isFinishing()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "Conquistas carregadas com sucesso.");
                    AchievementsResponse data = response.body();
                    Log.d(TAG, "Resposta da API: " + data.toString());

                    userLevel = data.getUserLevel();
                    totalPoints = data.getTotalPoints();

                    if (userLevel <= 0) userLevel = 1;
                    if (totalPoints < 0) totalPoints = 0;

                    updateHeaderInfo();

                    List<Achievement> fetchedAchievements = new ArrayList<>();
                    populateAchievements(fetchedAchievements, data.getAchievementProgress());

                    if (!fetchedAchievements.isEmpty()) {
                        achievementsList.clear();
                        achievementsList.addAll(fetchedAchievements);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "Lista de conquistas do servidor está vazia, usando predefinidas localmente");
                        loadLocalPredefinedAchievements();
                    }

                } else {
                    String errorMsg = "Erro ao carregar conquistas";
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMsg += ": " + errorBody;
                            try {
                                JSONObject errorJson = new JSONObject(errorBody);
                                Log.e(TAG, "Erro detalhado: " + errorJson.toString(2));
                            } catch (Exception jsonEx) {
                                Log.e(TAG, "Erro não é JSON válido: " + errorBody);
                            }
                        } else {
                            errorMsg += " (Código: " + response.code() + ")";
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler errorBody", e);
                    }
                    Log.e(TAG, "Erro ao carregar conquistas: " + errorMsg);

                    if (loadAttempts < MAX_LOAD_ATTEMPTS) {
                        Log.d(TAG, "Tentando carregar conquistas novamente em 1 segundo...");
                        new android.os.Handler().postDelayed(() -> loadAchievements(), 1000);
                    } else {
                        Toast.makeText(AchievementsActivity.this, "Usando dados locais de conquistas", Toast.LENGTH_SHORT).show();
                        loadLocalPredefinedAchievements();
                    }
                }
            }

            @Override
            public void onFailure(Call<AchievementsResponse> call, Throwable t) {
                if (isDestroyed() || isFinishing()) return;
                Log.e(TAG, "Falha de conexão ao carregar conquistas: " + t.getMessage(), t);

                if (loadAttempts < MAX_LOAD_ATTEMPTS) {
                    Log.d(TAG, "Tentando carregar conquistas novamente em 2 segundos...");
                    new android.os.Handler().postDelayed(() -> loadAchievements(), 2000);
                } else {
                    Toast.makeText(AchievementsActivity.this, "Usando dados locais de conquistas devido a problemas de conexão", Toast.LENGTH_SHORT).show();
                    loadLocalPredefinedAchievements();
                }
            }
        });
    }

    private void loadLocalPredefinedAchievements() {
        Log.d(TAG, "Carregando conquistas predefinidas localmente");

        SharedPreferences prefs = getSharedPreferences(USER_LOGIN_PREFS, MODE_PRIVATE);
        int safescore = prefs.getInt("safescore", 50);

        userLevel = (safescore / 100) + 1;
        totalPoints = safescore;

        updateHeaderInfo();

        List<Achievement> localAchievements = new ArrayList<>();
        localAchievements.add(new Achievement(1, "Viajante Iniciante", "Complete sua primeira viagem usando o app", 10, "ic_achievement_trip", "trip", 1, 1, true));
        localAchievements.add(new Achievement(2, "Viajante Experiente", "Complete 10 viagens com segurança", 25, "ic_achievement_trip", "trip", 10, Math.min(safescore / 10, 10), safescore >= 100));
        localAchievements.add(new Achievement(3, "Viajante Mestre", "Complete 50 viagens com segurança", 50, "ic_achievement_trip", "trip", 50, Math.min(safescore / 5, 50), safescore >= 250));
        localAchievements.add(new Achievement(4, "Checklist de Segurança", "Complete o checklist de segurança antes da viagem", 5, "ic_achievement_checklist", "checklist", 1, 1, true));
        localAchievements.add(new Achievement(5, "Sempre Alerta", "Complete o checklist de segurança 10 vezes", 20, "ic_achievement_checklist", "checklist", 10, Math.min(safescore / 10, 10), safescore >= 100));
        localAchievements.add(new Achievement(6, "Compartilhador Seguro", "Compartilhe sua rota com um contato de segurança", 15, "ic_achievement_share", "share", 1, Math.min(safescore / 50, 1), safescore >= 50));
        localAchievements.add(new Achievement(7, "Gravação Útil", "Autorize a gravação de áudio durante uma viagem", 15, "ic_achievement_audio", "audio", 1, Math.min(safescore / 50, 1), safescore >= 50));
        localAchievements.add(new Achievement(8, "Feedback Construtivo", "Envie feedback após 5 viagens", 25, "ic_achievement_feedback", "feedback", 5, Math.min(safescore / 20, 5), safescore >= 100));
        localAchievements.add(new Achievement(9, "Especialista em Segurança", "Acumule 100 pontos de SafeScore", 50, "ic_achievement_safety", "safety", 100, Math.min(safescore, 100), safescore >= 100));

        achievementsList.clear();
        achievementsList.addAll(localAchievements);
        adapter.notifyDataSetChanged();

        Toast.makeText(this, "Dados de conquistas carregados localmente", Toast.LENGTH_SHORT).show();
    }

    private void populateAchievements(List<Achievement> achievementListToPopulate, List<AchievementsResponse.AchievementProgress> progressList) {
        List<Achievement> allPredefinedAchievements = new ArrayList<>();
        allPredefinedAchievements.add(new Achievement(1, "Viajante Iniciante", "Complete sua primeira viagem usando o app", 10, "ic_achievement_trip", "trip", 1, 0, false));
        allPredefinedAchievements.add(new Achievement(2, "Viajante Experiente", "Complete 10 viagens com segurança", 25, "ic_achievement_trip", "trip", 10, 0, false));
        allPredefinedAchievements.add(new Achievement(3, "Viajante Mestre", "Complete 50 viagens com segurança", 50, "ic_achievement_trip", "trip", 50, 0, false));
        allPredefinedAchievements.add(new Achievement(4, "Checklist de Segurança", "Complete o checklist de segurança antes da viagem", 5, "ic_achievement_checklist", "checklist", 1, 0, false));
        allPredefinedAchievements.add(new Achievement(5, "Sempre Alerta", "Complete o checklist de segurança 10 vezes", 20, "ic_achievement_checklist", "checklist", 10, 0, false));
        allPredefinedAchievements.add(new Achievement(6, "Compartilhador Seguro", "Compartilhe sua rota com um contato de segurança", 15, "ic_achievement_share", "share", 1, 0, false));
        allPredefinedAchievements.add(new Achievement(7, "Gravação Útil", "Autorize a gravação de áudio durante uma viagem", 15, "ic_achievement_audio", "audio", 1, 0, false));
        allPredefinedAchievements.add(new Achievement(8, "Feedback Construtivo", "Envie feedback após 5 viagens", 25, "ic_achievement_feedback", "feedback", 5, 0, false));
        allPredefinedAchievements.add(new Achievement(9, "Especialista em Segurança", "Acumule 100 pontos de SafeScore", 50, "ic_achievement_safety", "safety", 100, 0, false));

        if (progressList == null || progressList.isEmpty()) {
            Log.d(TAG, "Nenhum progresso de conquista recebido da API. Usando todas as predefinidas com progresso 0.");
            achievementListToPopulate.addAll(allPredefinedAchievements);
            return;
        }

        Log.d(TAG, "Progresso recebido para " + progressList.size() + " conquistas.");

        for (Achievement predefinedAch : allPredefinedAchievements) {
            boolean progressFound = false;
            for (AchievementsResponse.AchievementProgress progress : progressList) {
                if (predefinedAch.getId() == progress.getAchievementId()) {
                    Log.d(TAG, "Progresso encontrado para Achievement ID: " + progress.getAchievementId() +
                            ", Progresso: " + progress.getProgress() + ", Completado: " + progress.isCompleted());
                    achievementListToPopulate.add(new Achievement(
                            predefinedAch.getId(),
                            predefinedAch.getTitle(),
                            predefinedAch.getDescription(),
                            predefinedAch.getPoints(),
                            predefinedAch.getIconResource(),
                            predefinedAch.getType(),
                            predefinedAch.getTarget(),
                            progress.getProgress(),
                            progress.isCompleted()
                    ));
                    progressFound = true;
                    break;
                }
            }
            if (!progressFound) {
                Log.d(TAG, "Nenhum progresso para Achievement ID: " + predefinedAch.getId() + ". Adicionando com progresso padrão.");
                achievementListToPopulate.add(predefinedAch);
            }
        }
        Log.d(TAG, "Lista final de conquistas para o adapter: " + achievementListToPopulate.size());
    }

    private void updateHeaderInfo() {
        if (textViewUserLevel == null || textViewTotalPoints == null || textViewPointsToNextLevel == null || progressBarNextLevel == null) {
            Log.e(TAG, "Um ou mais TextViews/ProgressBar do cabeçalho são nulos em updateHeaderInfo.");
            return;
        }
        textViewUserLevel.setText("Nível " + userLevel);
        textViewTotalPoints.setText(totalPoints + " pontos");

        int pointsForNextLevelTarget = userLevel * 100;
        int pointsNeeded = Math.max(0, pointsForNextLevelTarget - totalPoints);
        textViewPointsToNextLevel.setText("Faltam " + pointsNeeded + " pontos para o próximo nível");

        int pointsEarnedInCurrentLevel = totalPoints - ((userLevel -1) * 100);
        int progressPercentage = (pointsForNextLevelTarget > 0 && totalPoints < pointsForNextLevelTarget) ? (int) (((double)pointsEarnedInCurrentLevel / 100.0) * 100) : (totalPoints >= pointsForNextLevelTarget ? 100 : 0);
        if(totalPoints >= pointsForNextLevelTarget && pointsForNextLevelTarget > 0) progressPercentage = 100;
        if (pointsForNextLevelTarget == 0 && totalPoints == 0) progressPercentage = 0;

        progressBarNextLevel.setMax(100);
        progressBarNextLevel.setProgress(Math.min(progressPercentage, 100));

        Log.d(TAG, "updateHeaderInfo: level=" + userLevel +
                ", totalPoints=" + totalPoints +
                ", pointsForNextLevel=" + pointsForNextLevelTarget +
                ", pointsNeeded=" + pointsNeeded +
                ", pointsEarnedInLevel=" + pointsEarnedInCurrentLevel +
                ", progressPercentage=" + progressPercentage);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(AchievementsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        navigateToHome();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBottomNavigationSelection(R.id.navAchievements); // Garante que o item correto está selecionado
    }
}