package eu.orchestrator.transfer.util;

import org.apache.commons.codec.binary.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    final static Logger logger = Logger.getLogger(Util.class.getName());

    public enum ALGORITHM {

        SHA, MD5
    }

    // The generated strings end up in identifiers that are used as lookup keys, so they are drawn
    // from a cryptographically secure source rather than Math.random().
    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();

    public static enum Mode {

        ALPHA, ALPHANUMERIC, NUMERIC, SYMBOL, ALPHANUMERIC_IGNORE_CASE
    }

    private static SecretKeySpec secretKey;
    private static byte[] key;

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

    public static String decrypt(String strToDecrypt, String myKey) {
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
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }
        return decryptedString;
    }

    public static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    public static String createAlgorithm(String content, String algorithm) {

        StringBuilder hexString = new StringBuilder();

        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Util.class.getName()).log(Level.SEVERE, null, ex);
        }

        return hexString.toString();
    }

    /**
     * Read the object from Base64 string.
     */
    public static Object deserializeFromString(String s) throws IOException,
            ClassNotFoundException {
        byte[] data = java.util.Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    /**
     * Write the object to a Base64 string.
     */
    public static String serializeToString(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * Generates a new Random String for Password
     *
     * @param length
     * @param mode
     * @return A String object
     */
    public static String generateRandomString(int length, Mode mode) {

        StringBuffer buffer = new StringBuffer();
        String characters = "";

        switch (mode) {

            case ALPHA:
                characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                break;

            case ALPHANUMERIC:
                //characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$";
                characters = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ1234567890";
                break;

            case ALPHANUMERIC_IGNORE_CASE:
                //characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$";
                characters = "abcdefghijkmnpqrstuvwxyz1234567890";
                break;

            case NUMERIC:
                characters = "1234567890";
                break;

        }

        int charactersLength = characters.length();

        for (int i = 0; i < length; i++) {
            buffer.append(characters.charAt(RANDOM.nextInt(charactersLength)));
        }

        String randomString = buffer.toString();



        return randomString;
    } // EoM generateRandomPassword

    public static byte[] convertInputStreamToByteArray(InputStream _inputStream) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = _inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();

            return buffer.toByteArray();

        } // EoM convertByteArrayToInputStream
        catch (IOException ex) {
            Logger.getLogger(Util.class
                    .getName()).log(Level.SEVERE, null, ex);

            return null;
        }
    }

    /**
     * Computes SHA-256 hash of a string
     *
     * @param str
     * @return hash
     */
    public static String sha256hash(String str) {
        String hash = "";
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(str.getBytes("UTF-8"));
            byte[] digest = md.digest();
            hash = String.format("%064x", new java.math.BigInteger(1, digest)).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            logger.severe("Not a valid Hash Algorithm for string: " + str);
        } catch (UnsupportedEncodingException e) {
            logger.severe("Not a valid Encoding for string: " + str);
        }
        return hash;
    }

    public static List<String> findMatches(String text, String regularExpression) {

        List<String> matches = new ArrayList<>();

        Pattern pattern = Pattern.compile(regularExpression, Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(text);

        while (m.find()) {
            matches.add(m.group());
        }

        return matches;

    }

    public static String convertBinaryToBase64(byte[] image) throws IOException {

        return Base64.encodeBase64String(image);
    }

    public static String readFile(String file) {

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = null;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.getProperty("line.separator");


            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            if (null != reader) {
                reader.close();
            }

            return stringBuilder.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

//    public static String convertImageToThumbnail(byte[] mediaContent, String extension) {
//
//        String base64Thumbnail = null;
//
//        try {
//
//            BufferedImage originalBufferedImage = null;
//
//            originalBufferedImage = ImageIO.read(new ByteArrayInputStream(mediaContent));
//
//            BufferedImage thumbnailBufferedImage = Scalr.resize(originalBufferedImage, 250);
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            ImageIO.write(thumbnailBufferedImage, extension, baos);
//            baos.flush();
//            byte[] imageInByte = baos.toByteArray();
//            baos.close();
//
//            base64Thumbnail = org.apache.commons.codec.binary.Base64.encodeBase64String(imageInByte);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            logger.log(Level.SEVERE, e.getMessage(), e);
//        }
//
//        return base64Thumbnail;
//
//    }

    public static boolean isValidPassword(String password) {

        int errors = 0;

        if (null != password && !password.isEmpty()) {

            if (password.length() < 8) {
                errors++;
            }

            if (!Pattern.compile("(.*[0-9].*)").matcher(password).find()) {
                errors++;
            }

            if (!Pattern.compile("(.*[a-z].*)").matcher(password).find()) {
                errors++;
            }

            if (!Pattern.compile("(.*[A-Z].*)").matcher(password).find()) {
                errors++;
            }

            if (!Pattern.compile("(.*\\d.*)").matcher(password).find()) {
                errors++;
            }
        }

        if (errors > 0) {
            return false;
        } else {
            return true;
        }
    }
}
