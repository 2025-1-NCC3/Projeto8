package com.saulop.ubersafestartfecap.utils;

import android.util.Base64;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoHelper {
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM = "AES";
    // MESMA chave em hex do servidor, convertida aqui
    private static final byte[] KEY = hexStringToByteArray(
            "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff"
    );

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len/2];
        for (int i = 0; i < len; i += 2) {
            data[i/2] = (byte) ((Character.digit(s.charAt(i),16)<<4)
                    + Character.digit(s.charAt(i+1),16));
        }
        return data;
    }

    public static String encryptToBase64(String plain) throws Exception {
        // gera IV aleatório
        byte[] iv = new byte[16];
        java.security.SecureRandom.getInstanceStrong().nextBytes(iv);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, ALGORITHM),
                new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

        // prefixa IV + encrypted
        ByteBuffer bb = ByteBuffer.allocate(iv.length + encrypted.length);
        bb.put(iv);
        bb.put(encrypted);
        return Base64.encodeToString(bb.array(), Base64.NO_WRAP);
    }

    public static String decryptFromBase64(String b64) throws Exception {
        byte[] all = Base64.decode(b64, Base64.NO_WRAP);
        ByteBuffer bb = ByteBuffer.wrap(all);
        byte[] iv = new byte[16];
        bb.get(iv);
        byte[] encrypted = new byte[bb.remaining()];
        bb.get(encrypted);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, ALGORITHM),
                new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
