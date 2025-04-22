package com.saulop.ubersafestartfecap.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.saulop.ubersafestartfecap.api.ApiClient;
import com.saulop.ubersafestartfecap.api.AuthService;
import com.saulop.ubersafestartfecap.model.SafeScoreResponse;
import com.saulop.ubersafestartfecap.model.SafeScoreUpdate;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SafeScoreHelper {
    private static final String TAG = "SafeScoreHelper";

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

    public static void addTestPoints(Context context, int points) {
        updateSafeScore(context, points);
    }
}