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

        String token = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE)
                .getString("token", "");

        Log.d(TAG, "Token para trackAchievement: " + (token.isEmpty() ? "VAZIO!" : "OK (não vazio)"));

        if (token.isEmpty()) {
            Log.e(TAG, "Não foi possível rastrear conquista: usuário não está logado");
            trackAchievementLocally(context, type, increment);
            return;
        }

        Integer[] achievementIds = ACHIEVEMENT_TYPE_IDS.get(type);
        if (achievementIds == null || achievementIds.length == 0) {
            Log.e(TAG, "Tipo de conquista não encontrado: " + type);
            return;
        }

        final AtomicBoolean achievementCompletedAtomic = new AtomicBoolean(false);

        // Rastrear localmente e verificar se alguma conquista foi completada
        if (trackAchievementLocally(context, type, increment)) {
            achievementCompletedAtomic.set(true); // Marca que uma conquista foi completada localmente
        }


        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("type", type);
        requestBody.put("increment", increment);

        AuthService authService = ApiClient.getClient().create(AuthService.class);
        authService.trackAchievement("Bearer " + token, requestBody).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.d(TAG, "Conquista rastreada com sucesso no servidor: " + type + " +" + increment);

                    if (!achievementCompletedAtomic.get() && !isDialogShowing()) {
                        String typeTitle = ACHIEVEMENT_TYPE_TITLES.getOrDefault(type, type);
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
                                JSONObject errorJson = new JSONObject(errorBody);
                                Log.e(TAG, "Erro detalhado: " + errorJson.toString(2));
                            } catch (Exception jsonEx) {
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

    /**
     * Processa conquistas localmente e retorna true se alguma conquista foi completada.
     * @param context Contexto da aplicação.
     * @param type Tipo da conquista.
     * @param increment Incremento no progresso.
     * @return true se uma conquista foi desbloqueada, false caso contrário.
     */
    private static boolean trackAchievementLocally(Context context, String type, int increment) {
        boolean anyAchievementCompleted = false;
        SharedPreferences completedPrefs = context.getSharedPreferences(COMPLETED_ACHIEVEMENTS_PREFS, Context.MODE_PRIVATE);
        Integer[] achievementIds = ACHIEVEMENT_TYPE_IDS.get(type);

        if (achievementIds == null || achievementIds.length == 0) {
            Log.e(TAG, "Tipo de conquista não encontrado no processamento local: " + type);
            return false;
        }

        for (Integer achievementId : achievementIds) {
            boolean isCompleted = completedPrefs.getBoolean("achievement_" + achievementId, false);
            if (isCompleted) {
                Log.d(TAG, "Conquista " + achievementId + " já foi concluída. Pulando.");
                continue;
            }

            String progressKey = "progress_" + achievementId;
            int currentProgress = completedPrefs.getInt(progressKey, 0);
            int target = ACHIEVEMENT_TARGETS.getOrDefault(achievementId, 1);

            Log.d(TAG, "Progresso atual para conquista " + achievementId + ": " + currentProgress + "/" + target);
            int newProgress = currentProgress + increment;
            boolean newlyCompleted = newProgress >= target;

            SharedPreferences.Editor editor = completedPrefs.edit();
            editor.putInt(progressKey, newProgress);

            if (newlyCompleted) {
                editor.putBoolean("achievement_" + achievementId, true);
                editor.apply(); // Aplicar imediatamente para garantir que a notificação seja baseada no estado mais recente

                String title = ACHIEVEMENT_TITLES.getOrDefault(achievementId, "Conquista Desbloqueada!");
                int points = ACHIEVEMENT_POINTS.getOrDefault(achievementId, 10);

                showAchievementUnlockedDialog(context, title, points, type);
                SafeScoreHelper.updateSafeScore(context, points);
                anyAchievementCompleted = true;
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
        if (context == null || !(context instanceof Activity)) {
            Log.w(TAG, "Contexto inválido ou não é uma Activity para mostrar diálogo de conquista.");
            return;
        }
        Activity activity = (Activity) context;

        if (activity.isFinishing() || activity.isDestroyed()) {
            Log.w(TAG, "Activity está finalizando ou destruída. Não mostrando diálogo de conquista.");
            return;
        }

        // Garante que será executado na thread UI
        activity.runOnUiThread(() -> {
            // Fechar diálogo anterior se estiver aberto
            if (currentDialog != null && currentDialog.isShowing()) {
                try {
                    currentDialog.dismiss();
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao dispensar diálogo anterior: ", e);
                }
                currentDialog = null;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AlertDialogTheme);
            View achievementView = LayoutInflater.from(activity).inflate(R.layout.dialog_achievement_unlocked, null);

            TextView titleTextView = achievementView.findViewById(R.id.textViewAchievementTitle);
            TextView pointsTextView = achievementView.findViewById(R.id.textViewAchievementPoints);
            TextView descriptionTextView = achievementView.findViewById(R.id.textViewAchievementDescription); // Certifique-se que este ID existe no seu layout
            ImageView iconImageView = achievementView.findViewById(R.id.imageViewAchievementIcon);

            titleTextView.setText(title);
            pointsTextView.setText("+" + points);
            // Se você tiver uma descrição específica para a notificação (além do título da conquista)
            // descriptionTextView.setText("Você desbloqueou uma nova conquista!"); // Exemplo
            // Caso contrário, pode remover ou ocultar este TextView no layout/código.
            // Por agora, vou manter como no seu código original que seta "Conquista desbloqueada!"
            descriptionTextView.setText("Conquista desbloqueada!");


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
                    currentDialog.getWindow().setGravity(Gravity.TOP);
                }
                currentDialog.show();

                // Fechar automaticamente após 3 segundos
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // ***** INÍCIO DA CORREÇÃO *****
                    // A variável 'activity' aqui é a mesma que foi capturada do escopo externo.
                    // Verificamos o estado da Activity ANTES de tentar dispensar o diálogo.
                    if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                        if (currentDialog != null && currentDialog.isShowing()) {
                            try {
                                currentDialog.dismiss();
                            } catch (IllegalArgumentException e) {
                                Log.e(TAG, "Erro ao dispensar diálogo (IllegalArgumentException): View not attached to window manager.", e);
                            } catch (Exception e) {
                                Log.e(TAG, "Erro genérico ao dispensar diálogo: ", e);
                            }
                        }
                    } else {
                        Log.w(TAG, "Auto-dismiss do diálogo de conquista ignorado: A Activity está finalizando ou destruída.");
                    }
                    // ***** FIM DA CORREÇÃO *****
                }, 3000);
            } catch (Exception e) {
                Log.e(TAG, "Erro ao exibir diálogo de conquista", e);
            }
        });
    }


    /**
     * Reseta o progresso de uma conquista específica (para testes).
     */
    public static void resetAchievement(Context context, int achievementId) {
        if (context == null) return;

        SharedPreferences completedPrefs = context.getSharedPreferences(COMPLETED_ACHIEVEMENTS_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = completedPrefs.edit();

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
