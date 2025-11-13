package at.riemer.client.login;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class TokenPayloadHelper {

    private TokenPayloadHelper() {}

    public static String extractUsernameFromToken(String token) {
        if (token == null || token.isEmpty()) return null;

        int dot = token.indexOf('.');
        if (dot <= 0) return null;

        String payloadB64 = token.substring(0, dot);
        byte[] payloadBytes;
        try {
            payloadBytes = Base64.getDecoder().decode(payloadB64);
        } catch (IllegalArgumentException e) {
            return null;
        }

        String payload = new String(payloadBytes, StandardCharsets.UTF_8);
        // Erwartet: "username=jj;issuedAt=12345"
        String[] parts = payload.split(";");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("username=")) {
                return part.substring("username=".length());
            }
        }
        return null;
    }
}
