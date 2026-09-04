package eu.orchestrator.elasticity.spi.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import jakarta.persistence.EntityManager;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 8/8/2019
 */
public class Util {

    private static final Logger logger = Logger.getLogger(Util.class.getName());
    private static byte[] key;
    private static SecretKeySpec secretKey;


    public static String createRandomHEXString(EntityManager entityManager) {

        String hexID = eu.orchestrator.transfer.util.Util
                .generateRandomString(10, eu.orchestrator.transfer.util.Util.Mode.ALPHANUMERIC);
        return hexID;
    }

    public static String encrypt(String strToEncrypt, String myKey) {
        strToEncrypt = (null == strToEncrypt ? "" : strToEncrypt);
        MessageDigest sha = null;
        String encryptedString = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            encryptedString = new String(Base64.encodeBase64Chunked(cipher.doFinal(strToEncrypt.getBytes("UTF-8"))));

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return encryptedString;
    }

    public static String decrypt(String strToDecrypt,String myKey) {
        byte[] key;
        SecretKeySpec secretKey;
        MessageDigest sha = null;
        String decryptedString = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");

            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            decryptedString = new String(cipher.doFinal(Base64.decodeBase64(strToDecrypt.getBytes())));
        } catch (Exception exception) {
            logger.log(Level.SEVERE, exception.getMessage(), exception);
            exception.printStackTrace();
        }
        return decryptedString;
    }

    public static String stringHash(String inputString) {
        inputString = (null == inputString ? "" : inputString);

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException exception) {
            exception.printStackTrace();
        }
        messageDigest.update(inputString.getBytes());
        String hashingString = new String(messageDigest.digest());

        return null == hashingString ? "" : hashingString;
    }

}
