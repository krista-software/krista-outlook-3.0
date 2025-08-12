package app.krista.extensions.essentials.collaboration.outlook3.impl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class EncryptionUtil {

    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);
    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "MySecretKey12345"; // Use proper key management

    public static String encrypt(String plainText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception cause) {
            logger.error("Failed to secure client credentials: {}", cause.getMessage(), cause);
            throw new RuntimeException("Unable to secure your credentials. Please try again.", cause);
        }
    }

    public static String decrypt(String encryptedText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted);
        } catch (Exception cause) {
            logger.error("Failed to retrieve client credentials: {}", cause.getMessage(), cause);
            throw new RuntimeException("Unable to retrieve your credentials. Please re-authenticate.", cause);
        }
    }
}