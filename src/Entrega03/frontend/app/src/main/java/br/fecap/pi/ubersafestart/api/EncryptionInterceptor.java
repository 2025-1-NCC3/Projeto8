package br.fecap.pi.ubersafestart.api;

import android.util.Log;

import br.fecap.pi.ubersafestart.model.*;
import br.fecap.pi.ubersafestart.utils.CryptoHelper;
import okhttp3.*;
import okio.Buffer;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class EncryptionInterceptor implements Interceptor {
    private static final String TAG = "EncryptionInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // Log da requisição
        Log.d(TAG, "Enviando requisição para: " + original.url());

        // 1) Se houver corpo JSON, lê, cifra e envolve em { "data": "..." }
        RequestBody body = original.body();
        if (body != null && original.header("Content-Type") != null
                && original.header("Content-Type").contains("application/json")) {
            Buffer buf = new Buffer();
            body.writeTo(buf);
            String json = buf.readUtf8();

            // Log do corpo da requisição original
            Log.d(TAG, "Corpo da requisição: " + json);

            String encrypted;
            try {
                encrypted = CryptoHelper.encryptToBase64(json);
                Log.d(TAG, "Texto cifrado: " + encrypted.substring(0, Math.min(30, encrypted.length())) + "...");
            }
            catch (Exception e) {
                Log.e(TAG, "Erro ao cifrar requisição", e);
                throw new IOException(e);
            }

            JSONObject wrapper = new JSONObject();
            try {
                wrapper.put("data", encrypted);
            }
            catch (Exception e) {
                Log.e(TAG, "Erro ao criar wrapper JSON", e);
                throw new IOException(e);
            }

            body = RequestBody.create(
                    wrapper.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );
        }

        Request encryptedRequest = original.newBuilder()
                .method(original.method(), body)
                .build();

        // 2) Executa e obtém resposta
        Response response = chain.proceed(encryptedRequest);

        // Log da resposta
        Log.d(TAG, "Recebida resposta: " + response.code() + " " + response.message());

        // 3) Lê o JSON de resposta, decifra o campo "data" e devolve novo corpo
        ResponseBody respBody = response.body();
        if (respBody != null && respBody.contentType() != null &&
                respBody.contentType().subtype().equals("json")) {
            String respString = respBody.string();

            // Log do corpo da resposta cifrada
            Log.d(TAG, "Corpo da resposta cifrada: " + respString);

            try {
                JSONObject obj = new JSONObject(respString);

                if (!obj.has("data")) {
                    Log.w(TAG, "Campo 'data' não encontrado na resposta JSON. Retornando resposta original.");
                    // Recriar o body pois já consumimos o original
                    ResponseBody newBody = ResponseBody.create(
                            respString,
                            respBody.contentType()
                    );
                    return response.newBuilder().body(newBody).build();
                }

                String data = obj.getString("data");
                String decrypted = CryptoHelper.decryptFromBase64(data);

                // Log do corpo da resposta decifrada
                Log.d(TAG, "Corpo da resposta decifrada: " + decrypted);

                ResponseBody newBody = ResponseBody.create(
                        decrypted,
                        respBody.contentType()
                );
                return response.newBuilder().body(newBody).build();
            } catch (JSONException e) {
                Log.e(TAG, "Erro ao analisar JSON da resposta: " + respString, e);
                // Tentar retornar a resposta original mesmo com erro
                ResponseBody newBody = ResponseBody.create(
                        respString,
                        respBody.contentType()
                );
                return response.newBuilder().body(newBody).build();
            } catch (Exception e) {
                Log.e(TAG, "Falha ao decifrar response: " + e.getMessage(), e);
                // Em caso de erro na decifração, vamos formatar uma resposta de erro para o aplicativo
                String errorJson = "{\"success\":false,\"message\":\"Erro ao decifrar resposta: " +
                        e.getMessage().replace("\"", "'") + "\"}";
                ResponseBody newBody = ResponseBody.create(
                        errorJson,
                        MediaType.get("application/json; charset=utf-8")
                );
                return response.newBuilder().body(newBody).build();
            }
        }

        return response;
    }
}