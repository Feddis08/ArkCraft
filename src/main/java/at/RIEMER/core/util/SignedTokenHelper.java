package at.RIEMER.core.util;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public final class SignedTokenHelper {

    private static final String HMAC_ALGO = "HmacSHA256";

    private SignedTokenHelper() {}

    /**
     * Baut aus einem Payload-String und einem geheimen Schlüssel
     * einen signierten Token der Form:
     *
     *   base64(payload) + "." + base64(hmac)
     *
     * Beispiel payload: "username=Felix;issuedAt=1731450000"
     */
    public static String createSignedToken(String payload, String secretKey) {
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        byte[] mac = hmac(payloadBytes, secretKey.getBytes(StandardCharsets.UTF_8));

        String payloadB64 = Base64.getEncoder().encodeToString(payloadBytes);
        String macB64 = Base64.getEncoder().encodeToString(mac);

        return payloadB64 + "." + macB64;
    }

    /**
     * Prüft einen Token auf Gültigkeit.
     *
     * @return den Payload-String, wenn die Signatur korrekt ist
     * @throws IllegalArgumentException wenn der Token ungültig oder manipuliert ist
     */
    public static String verifySignedToken(String token, String secretKey) throws IllegalArgumentException {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Empty token");
        }

        int dot = token.indexOf('.');
        if (dot <= 0 || dot == token.length() - 1) {
            throw new IllegalArgumentException("Token format invalid");
        }

        String payloadB64 = token.substring(0, dot);
        String macB64 = token.substring(dot + 1);

        byte[] payloadBytes;
        byte[] macGiven;
        try {
            payloadBytes = Base64.getDecoder().decode(payloadB64);
            macGiven = Base64.getDecoder().decode(macB64);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Token base64 invalid", e);
        }

        byte[] macExpected = hmac(payloadBytes, secretKey.getBytes(StandardCharsets.UTF_8));

        if (!constantTimeEquals(macGiven, macExpected)) {
            throw new IllegalArgumentException("Token signature invalid");
        }

        return new String(payloadBytes, StandardCharsets.UTF_8);
    }

    // ---------------- intern ----------------

    private static byte[] hmac(byte[] data, byte[] key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(key, HMAC_ALGO);
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(keySpec);
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }

    /**
     * Vergleich in konstanter Zeit, um Timing-Angriffe zu vermeiden.
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null) return false;
        if (a.length != b.length) return false;

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= (a[i] ^ b[i]);
        }
        return result == 0;
    }
}
