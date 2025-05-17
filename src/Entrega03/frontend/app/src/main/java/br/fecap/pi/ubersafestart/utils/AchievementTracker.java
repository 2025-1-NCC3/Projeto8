package br.fecap.pi.ubersafestart.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import br.fecap.pi.ubersafestart.R;
import br.fecap.pi.ubersafestart.api.ApiClient;
import br.fecap.pi.ubersafestart.api.AuthService;
import br.fecap.pi.ubersafestart.model.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AchievementTracker {
    private static final String TAG = "AchievementTracker";
    private static AlertDialog currentDialog = null;

    // Nome da sharedPreference para armazenar conquistas já completadas
    private static final String COMPLETED_ACHIEVEMENTS_PREFS = "CompletedAchievements";

    // Mapeamento de tipos de conquistas para títulos amigáveis
    private static final Map<String, String> ACHIEVEMENT_TYPE_TITLES = new HashMap<>();
    static {
        ACHIEVEMENT_TYPE_TITLES.put("trip", "Viagem");
        ACHIEVEMENT_TYPE_TITLES.put("checklist", "Checklist de Segurança");
        ACHIEVEMENT_TYPE_TITLES.put("share", "Compartilhamento de Rota");
        ACHIEVEMENT_TYPE_TITLES.put("audio", "Gravação de Áudio");
        ACHIEVEMENT_TYPE_TITLES.put("feedback", "Feedback");
        ACHIEVEMENT_TYPE_TITLES.put("safety", "SafeScore");
    }

    // Mapeamento de tipos de conquistas para IDs de conquistas
    private static final Map<String, Integer[]> ACHIEVEMENT_TYPE_IDS = new HashMap<>();
    static {
        ACHIEVEMENT_TYPE_IDS.put("trip", new Integer[]{1, 2, 3}); // IDs 1, 2, 3 são conquistas de viagem
        ACHIEVEMENT_TYPE_IDS.put("checklist", new Integer[]{4, 5}); // IDs 4, 5 são conquistas de checklist
        ACHIEVEMENT_TYPE_IDS.put("share", new Integer[]{6}); // ID 6 é conquista de compartilhamento
        ACHIEVEMENT_TYPE_IDS.put("audio", new Integer[]{7}); // ID 7 é conquista de áudio
        ACHIEVEMENT_TYPE_IDS.put("feedback", new Integer[]{8}); // ID 8 é conquista de feedback
        ACHIEVEMENT_TYPE_IDS.put("safety", new Integer[]{9}); // ID 9 é conquista de SafeScore
    }

    // Mapeamento de IDs de conquistas para seus títulos
    private static final Map<Integer, String> ACHIEVEMENT_TITLES = new HashMap<>();
    static {
        ACHIEVEMENT_TITLES.put(1, "Viajante Iniciante");
        ACHIEVEMENT_TITLES.put(2, "Viajante Experiente");
        ACHIEVEMENT_TITLES.put(3, "Viajante Mestre");
        ACHIEVEMENT_TITLES.put(4, "Checklist de Segurança");
        ACHIEVEMENT_TITLES.put(5, "Sempre Alerta");
        ACHIEVEMENT_TITLES.put(6, "Compartilhador Seguro");
        ACHIEVEMENT_TITLES.put(7, "Gravação Útil");
        ACHIEVEMENT_TITLES.put(8, "Feedback Construtivo");
        ACHIEVEMENT_TITLES.put(9, "Especialista em Segurança");
    }

    // Mapeamento de IDs de conquistas para suas descrições
    private static final Map<Integer, String> ACHIEVEMENT_DESCRIPTIONS = new HashMap<>();
    static {
        ACHIEVEMENT_DESCRIPTIONS.put(1, "Complete sua primeira viagem usando o app");
        ACHIEVEMENT_DESCRIPTIONS.put(2, "Complete 10 viagens com segurança");
        ACHIEVEMENT_DESCRIPTIONS.put(3, "Complete 50 viagens com segurança");
        ACHIEVEMENT_DESCRIPTIONS.put(4, "Complete o checklist de segurança antes da viagem");
        ACHIEVEMENT_DESCRIPTIONS.put(5, "Complete o checklist de segurança 10 vezes");
        ACHIEVEMENT_DESCRIPTIONS.put(6, "Compartilhe sua rota com um contato de segurança");
        ACHIEVEMENT_DESCRIPTIONS.put(7, "Autorize a gravação de áudio durante uma viagem");
        ACHIEVEMENT_DESCRIPTIONS.put(8, "Envie feedback após 5 viagens");
        ACHIEVEMENT_DESCRIPTIONS.put(9, "Acumule 100 pontos de SafeScore");
    }

    // Mapeamento de IDs de conquistas para suas metas
    private static final Map<Integer, Integer> ACHIEVEMENT_TARGETS = new HashMap<>();
    static {
        ACHIEVEMENT_TARGETS.put(1, 1);
        ACHIEVEMENT_TARGETS.put(2, 10);
        ACHIEVEMENT_TARGETS.put(3, 50);
        ACHIEVEMENT_TARGETS.put(4, 1);
        ACHIEVEMENT_TARGETS.put(5, 10);
        ACHIEVEMENT_TARGETS.put(6, 1);
        ACHIEVEMENT_TARGETS.put(7, 1);
        ACHIEVEMENT_TARGETS.put(8, 5);
        ACHIEVEMENT_TARGETS.put(9, 100);
    }

    // Mapeamento de IDs de conquistas para seus pontos
    private static final Map<Integer, Integer> ACHIEVEMENT_POINTS = new HashMap<>();
    static {
        ACHIEVEMENT_POINTS.put(1, 10);
        ACHIEVEMENT_POINTS.put(2, 25);
        ACHIEVEMENT_POINTS.put(3, 50);
        ACHIEVEMENT_POINTS.put(4, 5);
        ACHIEVEMENT_POINTS.put(5, 20);
        ACHIEVEMENT_POINTS.put(6, 15);
        ACHIEVEMENT_POINTS.put(7, 15);
        ACHIEVEMENT_POINTS.put(8, 25);
        ACHIEVEMENT_POINTS.put(9, 50);
    }

    // Mapeamento de tipos de conquistas para ícones
    private static final Map<String, Integer> ACHIEVEMENT_TYPE_ICONS = new HashMap<>();
    static {
        ACHIEVEMENT_TYPE_ICONS.put("trip", R.drawable.ic_car);
        ACHIEVEMENT_TYPE_ICONS.put("checklist", R.drawable.ic_check_circle);
        ACHIEVEMENT_TYPE_ICONS.put("share", R.drawable.ic_share);
        ACHIEVEMENT_TYPE_ICONS.put("audio", R.drawable.ic_mic);
        ACHIEVEMENT_TYPE_ICONS.put("feedback", R.drawable.ic_star);
        ACHIEVEMENT_TYPE_ICONS.put("safety", R.drawable.ic_shield_check);
    }

    /**
     * Rastreia o progresso de conquistas de um tipo específico.
     *
     * @param context Contexto da atividade
     * @param type Tipo de conquista (trip, checklist, share, audio, feedback, safety)
     * @param increment Incremento a ser aplicado ao progresso (geralmente 1)
     */
    public static void trackAchievement(Context context, String type, int increment) {
        if (context == null) return;

        // Obter o token de autenticação
        String token = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
                .getString("token", "");

        // NOVO: Log do token (apenas se existe ou não)
        Log.d(TAG, "Token para trackAchievement: " + (token.isEmpty() ? "VAZIO!" : "OK (não vazio)"));

        if (token.isEmpty()) {
            Log.e(TAG, "Não foi possível rastrear conquista: usuário não está logado");

            // NOVO: Continuar com o rastreamento local mesmo sem token
            trackAchievementLocally(context, type, increment);
            return;
        }

        // Verificar se há conquistas deste tipo
        Integer[] achievementIds = ACHIEVEMENT_TYPE_IDS.get(type);
        if (achievementIds == null || achievementIds.length == 0) {
            Log.e(TAG, "Tipo de conquista não encontrado: " + type);
            return;
        }

        // Verificar quais conquistas já foram completadas
        SharedPreferences completedPrefs = context.getSharedPreferences(COMPLETED_ACHIEVEMENTS_PREFS, Context.MODE_PRIVATE);

        // Usar AtomicBoolean para contornar o problema de "effectively final"
        final AtomicBoolean achievementCompletedAtomic = new AtomicBoolean(false);

        // Rastrear localmente
        trackAchievementLocally(context, type, increment);
        if (achievementCompletedAtomic.get()) {
            // Se uma conquista foi completada localmente, não precisamos mostrar outra notificação do servidor
            Log.d(TAG, "Uma conquista já foi completada localmente, ignorando qualquer notificação do servidor.");
        }

        // Independentemente do progresso local, chamar a API para registrar no servidor
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("type", type);
        requestBody.put("increment", increment);

        // Chamar a API
        AuthService authService = ApiClient.getClient().create(AuthService.class);
        authService.trackAchievement("Bearer " + token, requestBody).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "Conquista rastreada com sucesso no servidor: " + type + " +" + increment);

                    // Mostrar um Toast de confirmação se não houver nenhuma conquista concluída neste momento
                    if (!achievementCompletedAtomic.get() && !isDialogShowing()) {
                        String typeTitle = ACHIEVEMENT_TYPE_TITLES.containsKey(type) ?
                                ACHIEVEMENT_TYPE_TITLES.get(type) : type;
                        Toast.makeText(context,
                                "Progresso em " + typeTitle + " atualizado!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Falha ao rastrear conquista no servidor: " + errorBody);

                            try {
                                // Tentar formatar como JSON para melhor legibilidade no log
                                JSONObject errorJson = new JSONObject(errorBody);
                                Log.e(TAG, "Erro detalhado: " + errorJson.toString(2));
                            } catch (Exception jsonEx) {
                                // Não é JSON válido, usar texto bruto
                                Log.e(TAG, "Erro bruto: " + errorBody);
                            }
                        } else {
                            Log.e(TAG, "Falha ao rastrear conquista. Código: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler resposta de erro", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Erro de rede ao rastrear conquista: " + t.getMessage(), t);
            }
        });
    }

    // NOVO: Método para processar conquistas localmente
    private static boolean trackAchievementLocally(Context context, String type, int increment) {
        boolean anyAchievementCompleted = false;

        // Verificar quais conquistas já foram completadas
        SharedPreferences completedPrefs = context.getSharedPreferences(COMPLETED_ACHIEVEMENTS_PREFS, Context.MODE_PRIVATE);

        // Verificar se há conquistas deste tipo
        Integer[] achievementIds = ACHIEVEMENT_TYPE_IDS.get(type);
        if (achievementIds == null || achievementIds.length == 0) {
            Log.e(TAG, "Tipo de conquista não encontrado no processamento local: " + type);
            return false;
        }

        // Rastrear localmente
        for (Integer achievementId : achievementIds) {
            // Verificar se a conquista já foi concluída
            boolean isCompleted = completedPrefs.getBoolean("achievement_" + achievementId, false);
            if (isCompleted) {
                Log.d(TAG, "Conquista " + achievementId + " já foi concluída. Pulando.");
                continue;
            }

            // Obter o progresso atual
            String progressKey = "progress_" + achievementId;
            int currentProgress = completedPrefs.getInt(progressKey, 0);
            int target = ACHIEVEMENT_TARGETS.containsKey(achievementId) ?
                    ACHIEVEMENT_TARGETS.get(achievementId) : 1;

            Log.d(TAG, "Progresso atual para conquista " + achievementId + ": " + currentProgress + "/" + target);

            // Incrementar o progresso
            int newProgress = currentProgress + increment;

            // Verificar se a conquista foi concluída
            boolean newlyCompleted = newProgress >= target;

            // Salvar o novo progresso
            SharedPreferences.Editor editor = completedPrefs.edit();
            editor.putInt(progressKey, newProgress);

            // Se foi concluída, marcar como concluída e mostrar notificação
            if (newlyCompleted) {
                editor.putBoolean("achievement_" + achievementId, true);
                editor.apply();

                // Pegar detalhes da conquista
                String title = ACHIEVEMENT_TITLES.containsKey(achievementId) ?
                        ACHIEVEMENT_TITLES.get(achievementId) :
                        "Conquista Desbloqueada!";
                int points = ACHIEVEMENT_POINTS.containsKey(achievementId) ?
                        ACHIEVEMENT_POINTS.get(achievementId) : 10;

                // Mostrar notificação
                showAchievementUnlockedDialog(context, title, points, type);

                // Atualizar o SafeScore
                SafeScoreHelper.updateSafeScore(context, points);

                // Sinalizar que uma conquista foi completada
                anyAchievementCompleted = true;

                // Interromper o loop após mostrar uma notificação para não sobrecarregar o usuário
                break;
            } else {
                Log.d(TAG, "Progresso atualizado para conquista " + achievementId + ": " + newProgress + "/" + target);
                editor.apply();
            }
        }

        return anyAchievementCompleted;
    }

    /**
     * Verifica se um diálogo de conquista está sendo exibido atualmente.
     */
    private static boolean isDialogShowing() {
        return currentDialog != null && currentDialog.isShowing();
    }

    /**
     * Exibe um diálogo informando que uma conquista foi desbloqueada.
     */
    private static void showAchievementUnlockedDialog(Context context, String title, int points, String type) {
        if (context == null || !(context instanceof Activity)) return;
        Activity activity = (Activity) context;

        if (activity.isFinishing() || activity.isDestroyed()) return;

        // Fechar diálogo anterior se estiver aberto
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogTheme);
        View achievementView = LayoutInflater.from(activity).inflate(R.layout.dialog_achievement_unlocked, null);

        TextView titleTextView = achievementView.findViewById(R.id.textViewAchievementTitle);
        TextView pointsTextView = achievementView.findViewById(R.id.textViewAchievementPoints);
        TextView descriptionTextView = achievementView.findViewById(R.id.textViewAchievementDescription);
        ImageView iconImageView = achievementView.findViewById(R.id.imageViewAchievementIcon);

        // Configurar detalhes no diálogo
        titleTextView.setText(title);
        pointsTextView.setText("+" + points);
        descriptionTextView.setText("Conquista desbloqueada!");

        // Definir o ícone com base no tipo
        Integer iconResId = ACHIEVEMENT_TYPE_ICONS.get(type);
        if (iconResId != null) {
            iconImageView.setImageResource(iconResId);
        }

        builder.setView(achievementView);
        builder.setCancelable(true);

        try {
            currentDialog = builder.create();
            if (currentDialog.getWindow() != null) {
                currentDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                // Posicionar o diálogo na parte superior da tela como uma notificação
                currentDialog.getWindow().setGravity(Gravity.TOP);
            }
            currentDialog.show();

            // Fechar automaticamente após 3 segundos
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (currentDialog != null && currentDialog.isShowing()) {
                    currentDialog.dismiss();
                }
            }, 3000);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao exibir diálogo de conquista", e);
        }
    }

    /**
     * Reseta o progresso de uma conquista específica (para testes).
     */
    public static void resetAchievement(Context context, int achievementId) {
        if (context == null) return;

        SharedPreferences completedPrefs = context.getSharedPreferences(COMPLETED_ACHIEVEMENTS_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = completedPrefs.edit();

        // Reset conquista
        editor.remove("achievement_" + achievementId);
        editor.remove("progress_" + achievementId);
        editor.apply();

        Log.d(TAG, "Conquista " + achievementId + " resetada para testes.");
        Toast.makeText(context, "Conquista " + achievementId + " resetada para testes", Toast.LENGTH_SHORT).show();
    }

    /**
     * Reseta todas as conquistas (para testes).
     */
    public static void resetAllAchievements(Context context) {
        if (context == null) return;

        SharedPreferences completedPrefs = context.getSharedPreferences(COMPLETED_ACHIEVEMENTS_PREFS, Context.MODE_PRIVATE);
        completedPrefs.edit().clear().apply();

        Log.d(TAG, "Todas as conquistas foram resetadas para testes.");
        Toast.makeText(context, "Todas as conquistas foram resetadas", Toast.LENGTH_SHORT).show();
    }

    /**
     * Obtém o progresso atual de uma conquista.
     */
    public static int getAchievementProgress(Context context, int achievementId) {
        if (context == null) return 0;

        SharedPreferences completedPrefs = context.getSharedPreferences(COMPLETED_ACHIEVEMENTS_PREFS, Context.MODE_PRIVATE);
        return completedPrefs.getInt("progress_" + achievementId, 0);
    }

    /**
     * Verifica se uma conquista está concluída.
     */
    public static boolean isAchievementCompleted(Context context, int achievementId) {
        if (context == null) return false;

        SharedPreferences completedPrefs = context.getSharedPreferences(COMPLETED_ACHIEVEMENTS_PREFS, Context.MODE_PRIVATE);
        return completedPrefs.getBoolean("achievement_" + achievementId, false);
    }
}