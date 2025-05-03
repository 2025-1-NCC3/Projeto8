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

        if (!prefs.contains("safescore")) {
            editor.putInt("safescore", 100); // Valor inicial
        }

        editor.apply();
        Log.d(TAG, "Dados de autenticação salvos para: " + username);
    }

    public static void updateSafeScore(Context context, int scoreChange) {
        SharedPreferences prefs = context.getSharedPreferences("userPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", "");

        if (token.isEmpty()) {
            Log.e(TAG, "Não foi possível atualizar o SafeScore: usuário não está logado");
            return;
        }

        SafeScoreUpdate update = new SafeScoreUpdate(scoreChange);

        // Chamar a API
        AuthService authService = ApiClient.getClient().create(AuthService.class);
        authService.updateSafeScore("Bearer " + token, update).enqueue(new Callback<SafeScoreResponse>() {
            @Override
            public void onResponse(Call<SafeScoreResponse> call, Response<SafeScoreResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Atualizar pontuação local
                    int newScore = response.body().getNewScore();
                    prefs.edit().putInt("safescore", newScore).apply();
                    Log.d(TAG, "SafeScore atualizado para: " + newScore);
                    Toast.makeText(context, "SafeScore atualizado: +" + scoreChange + " pontos!", Toast.LENGTH_SHORT).show();

                    // Mostrar a notificação personalizada se o contexto for uma Activity
                    if (context instanceof Activity) {
                        showSafeScoreNotification((Activity) context, scoreChange, newScore);
                    }
                } else {
                    try {
                        Log.e(TAG, "Falha ao atualizar SafeScore: " +
                                (response.errorBody() != null ? response.errorBody().string() : "Erro desconhecido"));
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao ler resposta de erro", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<SafeScoreResponse> call, Throwable t) {
                Log.e(TAG, "Erro de rede ao atualizar SafeScore: " + t.getMessage());
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