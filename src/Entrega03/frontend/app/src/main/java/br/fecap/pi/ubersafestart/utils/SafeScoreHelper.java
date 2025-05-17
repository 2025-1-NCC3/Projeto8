package br.fecap.pi.ubersafestart.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import br.fecap.pi.ubersafestart.R;
import br.fecap.pi.ubersafestart.api.ApiClient;
import br.fecap.pi.ubersafestart.api.AuthService;
import br.fecap.pi.ubersafestart.model.SafeScoreResponse;
import br.fecap.pi.ubersafestart.model.SafeScoreUpdate;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SafeScoreHelper {
    private static final String TAG = "SafeScoreHelper";
    private static PopupWindow popupWindow;

    public static void saveAuthData(Context context, String token, String username,
                                    String email, String type, String phone) {
        SharedPreferences prefs = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString("token", token);
        editor.putString("username", username);
        editor.putString("email", email);
        editor.putString("type", type);

        if (phone != null && !phone.isEmpty()) {
            editor.putString("phone", phone);
        }

        // CORREÇÃO: Inicializar com 0 em vez de 100, e depois buscar do servidor
        if (!prefs.contains("safescore")) {
            editor.putInt("safescore", 0); // Valor inicial
        }

        editor.apply();
        Log.d(TAG, "Dados de autenticação salvos para: " + username);

        // ADICIONADO: Buscar SafeScore do servidor após login
        fetchSafeScoreFromServer(context, token);
    }

    // NOVO MÉTODO: Busca SafeScore atual do servidor
    public static void fetchSafeScoreFromServer(Context context, String token) {
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token vazio ao tentar buscar SafeScore");
            return;
        }

        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;

        // Chamar a API para obter o SafeScore
        AuthService authService = ApiClient.getClient().create(AuthService.class);
        authService.getSafeScore(authToken).enqueue(new Callback<SafeScoreResponse>() {
            @Override
            public void onResponse(Call<SafeScoreResponse> call, Response<SafeScoreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SafeScoreResponse scoreData = response.body();
                    int score = scoreData.getSafescore();

                    Log.d(TAG, "SafeScore obtido do servidor: " + score);

                    // Salvar no SharedPreferences
                    SharedPreferences prefs = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
                    prefs.edit().putInt("safescore", score).apply();

                    // Não exibir notificação, só atualizar silenciosamente
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            Log.e(TAG, "Erro ao buscar SafeScore: " + errorJson);

                            try {
                                JSONObject errorObj = new JSONObject(errorJson);
                                Log.e(TAG, "Erro detalhado: " + errorObj.toString(2));
                            } catch (Exception e) {
                                Log.e(TAG, "Erro ao analisar resposta de erro");
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler corpo da resposta de erro", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<SafeScoreResponse> call, Throwable t) {
                Log.e(TAG, "Falha na conexão ao buscar SafeScore: " + t.getMessage(), t);
            }
        });
    }

    public static void updateSafeScore(Context context, int scoreChange) {
        SharedPreferences prefs = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");

        // ADICIONADO: Log do token para depuração
        Log.d(TAG, "Token para atualização de SafeScore: " + (token.isEmpty() ? "VAZIO!" : "OK"));

        if (token.isEmpty()) {
            Log.e(TAG, "Não foi possível atualizar o SafeScore: usuário não está logado");
            return;
        }

        // ADICIONADO: Log de SafeScore atual antes da atualização
        int currentScore = prefs.getInt("safescore", 0);
        Log.d(TAG, "Atualizando SafeScore. Atual: " + currentScore + ", Alteração: " + scoreChange);

        SafeScoreUpdate update = new SafeScoreUpdate(scoreChange);

        // Chamar a API
        AuthService authService = ApiClient.getClient().create(AuthService.class);
        authService.updateSafeScore("Bearer " + token, update).enqueue(new Callback<SafeScoreResponse>() {
            @Override
            public void onResponse(Call<SafeScoreResponse> call, Response<SafeScoreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Atualizar pontuação local
                    int newScore = response.body().getNewScore();

                    // CORREÇÃO: Garantir que o valor não seja zero se houver outro valor válido
                    if (newScore <= 0) {
                        newScore = response.body().getSafescore();
                    }

                    // CORREÇÃO: Log mais detalhado
                    Log.d(TAG, "SafeScore API response: newScore=" + response.body().getNewScore() +
                            ", safescore=" + response.body().getSafescore());

                    if (newScore <= 0) {
                        // Se ainda estiver zerado, calcular manualmente
                        newScore = currentScore + scoreChange;
                        Log.d(TAG, "Calculando SafeScore manualmente: " + currentScore + " + " +
                                scoreChange + " = " + newScore);
                    }

                    // Garantir que nunca seja negativo
                    newScore = Math.max(0, newScore);

                    // Salvar o novo valor
                    prefs.edit().putInt("safescore", newScore).apply();
                    Log.d(TAG, "SafeScore atualizado para: " + newScore);

                    // Notificar o usuário
                    Toast.makeText(context, "SafeScore atualizado: " +
                            (scoreChange >= 0 ? "+" : "") + scoreChange + " pontos!", Toast.LENGTH_SHORT).show();

                    // Mostrar a notificação personalizada se o contexto for uma Activity
                    if (context instanceof Activity) {
                        showSafeScoreNotification((Activity) context, scoreChange, newScore);
                    }
                } else {
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            Log.e(TAG, "Falha ao atualizar SafeScore: " + errorJson);

                            try {
                                JSONObject errorObj = new JSONObject(errorJson);
                                Log.e(TAG, "Erro detalhado: " + errorObj.toString(2));
                            } catch (Exception e) {
                                Log.e(TAG, "Erro ao analisar resposta de erro");
                            }
                        } else {
                            Log.e(TAG, "Erro desconhecido. Código: " + response.code());
                        }

                        // ADICIONADO: Atualização local como fallback em caso de erro na API
                        int fallbackScore = currentScore + scoreChange;
                        fallbackScore = Math.max(0, fallbackScore);
                        prefs.edit().putInt("safescore", fallbackScore).apply();
                        Log.d(TAG, "SafeScore atualizado localmente para: " + fallbackScore + " após falha na API");

                        // Mostrar uma mensagem menos assustadora para o usuário
                        Toast.makeText(context, "SafeScore atualizado localmente: " +
                                (scoreChange >= 0 ? "+" : "") + scoreChange + " pontos!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler resposta de erro", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<SafeScoreResponse> call, Throwable t) {
                Log.e(TAG, "Erro de rede ao atualizar SafeScore: " + t.getMessage(), t);

                // ADICIONADO: Atualização local em caso de falha de rede
                int fallbackScore = currentScore + scoreChange;
                fallbackScore = Math.max(0, fallbackScore);
                prefs.edit().putInt("safescore", fallbackScore).apply();
                Log.d(TAG, "SafeScore atualizado localmente para: " + fallbackScore + " após erro de rede");

                Toast.makeText(context, "SafeScore atualizado localmente: " +
                        (scoreChange >= 0 ? "+" : "") + scoreChange + " pontos!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void showSafeScoreNotification(Activity activity, int points, int totalScore) {
        // Fechar popup anterior se existir
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }

        try {
            // Inflar o layout da notificação
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popupView = inflater.inflate(R.layout.layout_safescore_notification, null);

            // Configurar o texto com os pontos ganhos e o total
            TextView textViewScoreChange = popupView.findViewById(R.id.textViewScoreChange);
            TextView textViewCurrentScore = popupView.findViewById(R.id.textViewCurrentScore);

            if (points > 0) {
                textViewScoreChange.setText("+" + points + " pontos");
                textViewScoreChange.setTextColor(Color.parseColor("#2979FF")); // Azul do Uber para pontos positivos
            } else {
                textViewScoreChange.setText(points + " pontos");
                textViewScoreChange.setTextColor(Color.parseColor("#FF5252")); // Vermelho para pontos negativos
            }

            textViewCurrentScore.setText(totalScore + "/100");

            // Criar o popup
            popupWindow = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );

            // Configurar animação
            popupWindow.setAnimationStyle(android.R.style.Animation_Toast);

            // Tornar o fundo visível
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            // Garantir que a janela atual tem foco
            popupWindow.setFocusable(false);
            popupWindow.setOutsideTouchable(true);

            // Mostrar o popup na parte superior da tela
            View rootView = activity.getWindow().getDecorView().getRootView();
            popupWindow.showAtLocation(rootView, Gravity.TOP, 0, 0);

            // Fechar automaticamente após 3 segundos
            new Handler().postDelayed(() -> {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }, 3000);

            // Log para debug
            Log.d(TAG, "Notification displayed: Points=" + points + ", Total=" + totalScore);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao mostrar notificação do SafeScore", e);
        }
    }

    public static void addTestPoints(Context context, int points) {
        updateSafeScore(context, points);
    }
}