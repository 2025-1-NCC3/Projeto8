package br.fecap.pi.ubersafestart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList; // Importar ColorStateList
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

import java.util.ArrayList;
import java.util.List;

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

    // MODIFICAÇÃO: Adicionar referências diretas aos Ícones e Textos da Barra de Navegação
    private ImageView iconHome, iconServices, iconAchievements, iconAccount;
    private TextView textHome, textServices, textAchievements, textAccount;

    // Array de IDs para facilitar a atualização da barra de navegação
    // Estes IDs devem corresponder aos IDs no seu XML (ex: activity_achievements.xml ou um layout incluído)
    // Assumindo que você tem um layout de rodapé similar ao activity_home.xml em activity_achievements.xml
    // Se os IDs forem diferentes em activity_achievements.xml, ajuste aqui.
    // Usaremos os IDs de HomeActivity como referência, mas idealmente seriam IDs próprios ou de um include.
    private final int[] navIconIds = {R.id.iconHome, R.id.iconServices, R.id.iconAchievements, R.id.iconAccount};
    private final int[] navTextIds = {R.id.textHome, R.id.textServices, R.id.textAchievements, R.id.textAccount};


    // Data
    private List<Achievement> achievementsList = new ArrayList<>(); // Renomeado para evitar confusão com o LinearLayout navAchievements
    private AchievementsAdapter adapter;
    private AuthService authService;
    private int totalPoints = 0;
    private int userLevel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements); // Certifique-se que este layout contém a barra de navegação

        // Initialize views
        initViews();

        // Initialize API service
        authService = ApiClient.getClient().create(AuthService.class);

        // Setup RecyclerView
        recyclerViewAchievements.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AchievementsAdapter(this, achievementsList);
        recyclerViewAchievements.setAdapter(adapter);

        // Setup navigation
        setupNavigationListeners();
        updateBottomNavigationSelection(R.id.navAchievements); // DESTAQUE INICIAL

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
        navAchievements = findViewById(R.id.navAchievements); // Este é o LinearLayout clicável
        navAccount = findViewById(R.id.navAccount);

        // MODIFICAÇÃO: Inicializar Ícones e Textos (assumindo que os IDs são os mesmos de activity_home.xml)
        // Se a sua activity_achievements.xml tiver IDs diferentes para estes componentes, ajuste-os aqui.
        // Se a barra de navegação for um layout incluído, os IDs devem estar nele.
        iconHome = findViewById(R.id.iconHome);
        textHome = findViewById(R.id.textHome);
        iconServices = findViewById(R.id.iconServices);
        textServices = findViewById(R.id.textServices);
        iconAchievements = findViewById(R.id.iconAchievements); // Este é o ImageView dentro do LinearLayout navAchievements
        textAchievements = findViewById(R.id.textAchievements); // Este é o TextView dentro do LinearLayout navAchievements
        iconAccount = findViewById(R.id.iconAccount);
        textAccount = findViewById(R.id.textAccount);
    }

    private void setupNavigationListeners() {
        View.OnClickListener listener = v -> {
            int id = v.getId();
            Log.d(TAG, "Item de navegação clicado: " + getResources().getResourceEntryName(id));

            if (id == R.id.navHome) {
                navigateToHome(); // Animação será aplicada aqui
            } else if (id == R.id.navServices) {
                Toast.makeText(AchievementsActivity.this, "Opções em desenvolvimento", Toast.LENGTH_SHORT).show();
                updateBottomNavigationSelection(R.id.navServices); // Manter destaque se navegar para cá
            } else if (id == R.id.navAchievements) {
                // Já está na tela de Conquistas
                Toast.makeText(AchievementsActivity.this, "Você já está na tela de Conquistas", Toast.LENGTH_SHORT).show();
                updateBottomNavigationSelection(R.id.navAchievements); // Reafirma o destaque
            } else if (id == R.id.navAccount) {
                Intent intent = new Intent(AchievementsActivity.this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left); // Animação padrão para a direita
                // Não precisa chamar updateBottomNavigationSelection aqui, pois ProfileActivity fará isso por si.
            }
        };

        if (navHome != null) navHome.setOnClickListener(listener); else Log.e(TAG, "navHome nulo");
        if (navServices != null) navServices.setOnClickListener(listener); else Log.e(TAG, "navServices nulo");
        if (navAchievements != null) navAchievements.setOnClickListener(listener); else Log.e(TAG, "navAchievements (LinearLayout) nulo");
        if (navAccount != null) navAccount.setOnClickListener(listener); else Log.e(TAG, "navAccount nulo");
    }

    private void navigateToHome() {
        SharedPreferences loginPrefs = getSharedPreferences(USER_LOGIN_PREFS, MODE_PRIVATE);
        String userType = loginPrefs.getString("type", "");
        Intent intent;

        if ("driver".equalsIgnoreCase(userType)) {
            intent = new Intent(AchievementsActivity.this, DriverHomeActivity.class);
        } else {
            intent = new Intent(AchievementsActivity.this, HomeActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP); // Adicionado CLEAR_TOP para melhor navegação
        startActivity(intent);
        // MODIFICAÇÃO: Animação slide_in_left ao ir para Home
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right); // slide_out_right para a tela atual sair para a direita
    }

    // MODIFICAÇÃO: Método para atualizar a seleção da barra de navegação
    private void updateBottomNavigationSelection(int selectedItemId) {
        LinearLayout[] navItems = {navHome, navServices, navAchievements, navAccount};
        ImageView[] navIcons = {iconHome, iconServices, iconAchievements, iconAccount};
        TextView[] navTexts = {textHome, textServices, textAchievements, textAccount};

        int activeColor = ContextCompat.getColor(this, R.color.white_fff); // Cor branca para ativo
        int inactiveColor = ContextCompat.getColor(this, R.color.gray_light); // Cor cinza para inativo

        for (int i = 0; i < navItems.length; i++) {
            LinearLayout itemLayout = navItems[i];
            ImageView icon = navIcons[i];
            TextView text = navTexts[i];

            if (itemLayout == null || icon == null || text == null) {
                Log.w(TAG, "Componente de navegação nulo na posição " + i);
                continue;
            }

            boolean isActive = (itemLayout.getId() == selectedItemId);

            icon.setImageTintList(ColorStateList.valueOf(isActive ? activeColor : inactiveColor));
            text.setTextColor(isActive ? activeColor : inactiveColor);
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

        authService.getUserAchievements(authHeader).enqueue(new Callback<AchievementsResponse>() {
            @Override
            public void onResponse(Call<AchievementsResponse> call, Response<AchievementsResponse> response) {
                if (isDestroyed() || isFinishing()) return; // Evita crash se a activity for destruída

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "Conquistas carregadas com sucesso.");
                    AchievementsResponse data = response.body();

                    userLevel = data.getUserLevel();
                    totalPoints = data.getTotalPoints();
                    updateHeaderInfo();

                    List<Achievement> fetchedAchievements = new ArrayList<>();
                    populateAchievements(fetchedAchievements, data.getAchievementProgress());

                    achievementsList.clear();
                    achievementsList.addAll(fetchedAchievements);
                    adapter.notifyDataSetChanged();

                } else {
                    String errorMsg = "Erro ao carregar conquistas";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        } else {
                            errorMsg += " (Código: " + response.code() + ")";
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler errorBody", e);
                    }
                    Toast.makeText(AchievementsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro ao carregar conquistas: " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<AchievementsResponse> call, Throwable t) {
                if (isDestroyed() || isFinishing()) return;
                Toast.makeText(AchievementsActivity.this, "Falha na conexão: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Falha de conexão ao carregar conquistas: " + t.getMessage(), t);
            }
        });
    }

    private void populateAchievements(List<Achievement> achievementList, List<AchievementsResponse.AchievementProgress> progressList) {
        // Lista de todas as conquistas predefinidas (base)
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
            achievementList.addAll(allPredefinedAchievements); // Adiciona todas com progresso padrão 0
            return;
        }

        Log.d(TAG, "Progresso recebido para " + progressList.size() + " conquistas.");

        // Mapeia o progresso para as conquistas predefinidas
        for (Achievement predefinedAch : allPredefinedAchievements) {
            boolean progressFound = false;
            for (AchievementsResponse.AchievementProgress progress : progressList) {
                if (predefinedAch.getId() == progress.getAchievementId()) {
                    Log.d(TAG, "Progresso encontrado para Achievement ID: " + progress.getAchievementId() +
                            ", Progresso: " + progress.getProgress() + ", Completado: " + progress.isCompleted());
                    achievementList.add(new Achievement(
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
                // Se não houver progresso para uma conquista predefinida, adiciona-a com progresso 0
                Log.d(TAG, "Nenhum progresso para Achievement ID: " + predefinedAch.getId() + ". Adicionando com progresso padrão.");
                achievementList.add(predefinedAch); // Mantém a conquista na lista, mesmo sem progresso da API
            }
        }
        Log.d(TAG, "Lista final de conquistas para o adapter: " + achievementList.size());
    }


    private void updateHeaderInfo() {
        if (textViewUserLevel == null || textViewTotalPoints == null || textViewPointsToNextLevel == null || progressBarNextLevel == null) {
            Log.e(TAG, "Um ou mais TextViews/ProgressBar do cabeçalho são nulos em updateHeaderInfo.");
            return;
        }
        textViewUserLevel.setText("Nível " + userLevel);
        textViewTotalPoints.setText(totalPoints + " pontos");

        int pointsForNextLevelTarget = userLevel * 100; // Meta de pontos para o próximo nível
        int pointsNeeded = Math.max(0, pointsForNextLevelTarget - totalPoints); // Garante que não seja negativo
        textViewPointsToNextLevel.setText("Faltam " + pointsNeeded + " pontos para o próximo nível");

        // Calcula o progresso dentro do nível atual
        int pointsEarnedInCurrentLevel = totalPoints - ((userLevel -1) * 100);
        int progressPercentage = (pointsForNextLevelTarget > 0 && totalPoints < pointsForNextLevelTarget) ? (int) (( (double)pointsEarnedInCurrentLevel / 100.0) * 100) : (totalPoints >= pointsForNextLevelTarget ? 100 : 0) ;
        if(totalPoints >= pointsForNextLevelTarget && pointsForNextLevelTarget > 0) progressPercentage = 100; // Caso tenha atingido ou ultrapassado
        if (pointsForNextLevelTarget == 0 && totalPoints == 0) progressPercentage = 0; // Nível 1, 0 pontos

        progressBarNextLevel.setMax(100); // A barra sempre representa 100 pontos para o próximo nível
        progressBarNextLevel.setProgress(Math.min(progressPercentage, 100)); // Limita o progresso a 100
    }

    private void navigateToLogin() {
        Intent intent = new Intent(AchievementsActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // MODIFICAÇÃO: Chama navigateToHome() para aplicar a animação correta
        navigateToHome();
        // super.onBackPressed(); // Não chamar super se já estamos tratando a navegação
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateBottomNavigationSelection(R.id.navAchievements);

        loadAchievements();
    }
}