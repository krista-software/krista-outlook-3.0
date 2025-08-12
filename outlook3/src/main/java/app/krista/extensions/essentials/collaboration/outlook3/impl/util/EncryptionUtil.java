package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class EncryptionUtil {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16; // AES block size
    private static final String SECRET_KEY = "Kri$Ta_9x!72@_SeCuRe#2025^LmNpQr";
    public static final String KRISTA_PREFIX = "Krista_";

    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("Plain text cannot be null or empty");
        }

        try {
            Cipher cipher = createCipher();
            byte[] iv = generateIV();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, createKeySpec(), ivSpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedWithIv = combineIvAndData(iv, encrypted);
            return KRISTA_PREFIX + Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception cause) {
            logger.error("Failed to secure client credentials: {}", cause.getMessage(), cause);
            throw new RuntimeException("Unable to secure your credentials. Please try again.", cause);
        }
    }

    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            throw new IllegalArgumentException("Encrypted text cannot be null or empty");
        }

        try {
            String cleanText = encryptedText.replace(KRISTA_PREFIX, "");
            byte[] encryptedWithIv = Base64.getDecoder().decode(cleanText);

            if (encryptedWithIv.length < IV_SIZE) {
                throw new IllegalArgumentException("Invalid encrypted data length");
            }

            byte[] iv = extractIV(encryptedWithIv);
            byte[] encrypted = extractEncryptedData(encryptedWithIv);

            Cipher cipher = createCipher();
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, createKeySpec(), ivSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception cause) {
            logger.error("Failed to retrieve client credentials: {}", cause.getMessage(), cause);
            throw new RuntimeException("Unable to retrieve your credentials. Please re-authenticate.", cause);
        }
    }

    private static Cipher createCipher() throws Exception {
        return Cipher.getInstance(TRANSFORMATION);
    }

    private static SecretKeySpec createKeySpec() {
        return new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    private static byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    private static byte[] combineIvAndData(byte[] iv, byte[] encrypted) {
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
        return combined;
    }

    private static byte[] extractIV(byte[] encryptedWithIv) {
        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(encryptedWithIv, 0, iv, 0, IV_SIZE);
        return iv;
    }

    private static byte[] extractEncryptedData(byte[] encryptedWithIv) {
        byte[] encrypted = new byte[encryptedWithIv.length - IV_SIZE];
        System.arraycopy(encryptedWithIv, IV_SIZE, encrypted, 0, encrypted.length);
        return encrypted;
    }
}
