/*
 * Outlook 3.0 Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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

    private static final int MAX_BASENAME_LEN = 100;
    private static final String SAFE_CHARS_REGEX = "[^A-Za-z0-9._ -]";

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