package com.saulop.ubersafestartfecap.api;

import com.saulop.ubersafestartfecap.utils.CryptoHelper;
import okhttp3.*;
import okio.Buffer;
import org.json.JSONObject;
import java.io.IOException;

public class EncryptionInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        // 1) Se houver corpo JSON, lê, cifra e envolve em { "data": "..." }
        RequestBody body = original.body();
        if (body != null && original.header("Content-Type") != null
                && original.header("Content-Type").contains("application/json")) {
            Buffer buf = new Buffer();
            body.writeTo(buf);
            String json = buf.readUtf8();

            String encrypted;
            try { encrypted = CryptoHelper.encryptToBase64(json); }
            catch (Exception e) { throw new IOException(e); }

            JSONObject wrapper = new JSONObject();
            try { wrapper.put("data", encrypted); }
            catch (Exception e) { throw new IOException(e); }

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

        // 3) Lê o JSON de resposta, decifra o campo "data" e devolve novo corpo
        ResponseBody respBody = response.body();
        if (respBody != null && respBody.contentType().subtype().equals("json")) {
            String respString = respBody.string();
            try {
                JSONObject obj = new JSONObject(respString);
                String data = obj.getString("data");
                String decrypted = CryptoHelper.decryptFromBase64(data);
                ResponseBody newBody = ResponseBody.create(
                        decrypted,
                        respBody.contentType()
                );
                return response.newBuilder().body(newBody).build();
            } catch (Exception e) {
                throw new IOException("Falha ao decifrar response", e);
            }
        }

        return response;
    }
}
