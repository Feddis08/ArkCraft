package at.riemer.core.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public final class FileHelper {

    private FileHelper() {}

    // ===========================
    // Lesen
    // ===========================
    public static String readFileUtf8(String path) throws IOException {
        return readFileUtf8(Paths.get(path));
    }

    public static String readFileUtf8(Path path) throws IOException {
        byte[] bytes = Files.readAllBytes(path);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // ===========================
    // Schreiben (überschreiben)
    // ===========================
    public static void writeFileUtf8(String path, String text) throws IOException {
        writeFileUtf8(Paths.get(path), text);
    }

    public static void writeFileUtf8(Path path, String text) throws IOException {
        ensureParentDirs(path);

        Files.write(
                path,
                text.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        );
    }

    // ===========================
    // Anhängen (append)
    // ===========================
    public static void appendFileUtf8(String path, String text) throws IOException {
        appendFileUtf8(Paths.get(path), text);
    }

    public static void appendFileUtf8(Path path, String text) throws IOException {
        ensureParentDirs(path);

        Files.write(
                path,
                text.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
        );
    }

    // ===========================
    // Hilfsfunktion
    // ===========================
    private static void ensureParentDirs(Path path) throws IOException {
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
    }
}
