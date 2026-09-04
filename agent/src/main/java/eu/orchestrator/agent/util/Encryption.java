package eu.orchestrator.agent.util;

import eu.orchestrator.agent.configuration.AgentConfiguration;

import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


public class Encryption {

    private static final Logger logger = Logger.getLogger(Encryption.class.getName());

    public static String decryption(String strToDecrypt) {
        String myKey = AgentConfiguration.encryptionKey();
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

    public static String encrypt(String strToEncrypt) {
        strToEncrypt = (null == strToEncrypt ? "" : strToEncrypt);
        String myKey = AgentConfiguration.encryptionKey();
        byte[] key;
        SecretKeySpec secretKey;
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

        } catch (Exception exception) {
            logger.log(Level.SEVERE, exception.getMessage(), exception);
            exception.printStackTrace();
        }

        return encryptedString;
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
