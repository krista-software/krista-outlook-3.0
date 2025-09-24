package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

import java.text.Normalizer;
import java.util.Objects;
import java.util.UUID;

/**
 * Utility to sanitize filenames for cross-platform safety and ASCII-only storage.
 * - Normalizes Unicode (NFC)
 * - Removes control and path characters
 * - Replaces any non [A-Za-z0-9._ -] with underscore
 * - Preserves extension and enforces a reasonable max length
 */
public final class FilenameUtil {

    private static final int MAX_BASENAME_LEN = 100; // keep headroom for extension
    private static final String SAFE_CHARS_REGEX = "[^A-Za-z0-9._ -]"; // anything not in whitelist

    private FilenameUtil() {}

    public static String toSafeFilename(String original) {
        String name = Objects.toString(original, "").trim();
        if (name.isEmpty()) {
            return "attachment";
        }

        // Unicode normalize
        name = Normalizer.normalize(name, Normalizer.Form.NFC);

        // Split extension (last dot that is not the first character)
        int lastDot = name.lastIndexOf('.');
        String base = (lastDot > 0) ? name.substring(0, lastDot) : name;
        String ext = (lastDot > 0 && lastDot < name.length() - 1) ? name.substring(lastDot + 1) : "";

        // Remove control characters and reserved/path characters in base
        base = base
                .replaceAll("[\\p{Cntrl}]", "_")
                .replaceAll("[<>:\"/\\\\|?*]", "_")
                .replaceAll(SAFE_CHARS_REGEX, "_")
                .replaceAll("[ ]{2,}", " ")
                .replaceAll("_+", "_")
                .replaceAll("^\\.+|\\.+$", "") // trim leading/trailing dots
                .trim();

        if (base.isEmpty()) {
            base = "attachment";
        }

        // Truncate base name to max length
        if (base.length() > MAX_BASENAME_LEN) {
            base = base.substring(0, MAX_BASENAME_LEN);
        }

        // Clean extension similarly (very conservatively)
        if (!ext.isEmpty()) {
            ext = ext
                    .replaceAll("[\\p{Cntrl}]", "")
                    .replaceAll("[<>:\"/\\\\|?*]", "")
                    .replaceAll(SAFE_CHARS_REGEX, "")
                    .replaceAll("^\\.+|\\.+$", "")
                    .trim();
        }

        return ext.isEmpty() ? base : base + "." + ext;
    }

    public static String uuidFallback(String original) {
        // Preserve sanitized extension, but ensure a safe UUID-based name
        String safe = toSafeFilename(original);
        int lastDot = safe.lastIndexOf('.');
        String ext = (lastDot > 0 && lastDot < safe.length() - 1) ? safe.substring(lastDot) : "";
        return "file-" + UUID.randomUUID().toString().substring(0, 8) + ext;
    }
}