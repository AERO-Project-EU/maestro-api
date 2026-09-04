package eu.orchestrator.backend.util;

import eu.orchestrator.repository.dao.NotificationDAO;
import eu.orchestrator.repository.domain.Notification;
import eu.orchestrator.repository.domain.NotificationConfiguration;
import eu.orchestrator.repository.domain.NotificationTemplate;
import eu.orchestrator.repository.domain.User;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.codec.binary.Base64;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public final class Util {

    private static final Logger logger = Logger.getLogger(Util.class.getName());
    private static final String NOTIFICATION_TOPIC = "/notifications";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static SecretKeySpec secretKey;
    private static byte[] key;

    public static boolean sendEmail(String emailTo, String emailMSG,
            NotificationTemplate notificationTemplate,
            NotificationConfiguration notificationConfiguration) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", notificationConfiguration.isSmtpAuth());
        props.put("mail.smtp.starttls.enable", notificationConfiguration.isSmtpTls());
        props.put("mail.smtp.host", notificationConfiguration.getSmtpHost());
        props.put("mail.smtp.port", notificationConfiguration.getSmtpPort());

        // Get the Session object.
        Session session = Session.getInstance(props,
                new jakarta.mail.Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(notificationConfiguration.getSmtpUsername(),
                                notificationConfiguration.getSmtpPassword());
                    }
                });

        try {

            // Create a default MimeMessage object.
            Message message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(notificationTemplate.getNotificationFrom()));

            message.setHeader("Content-Type", "text/html; charset=UTF-8");

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo));

            // Set Subject: header field
            message.setSubject(notificationTemplate.getNotificationSubject());

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(emailMSG, "text/html; charset=UTF-8");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);
            Transport.send(message);

        } catch (MessagingException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }

        return true;

    }

    public static boolean sendPushNotification(String entityID, String message, String clazz,
            String redirectURL, User user, String componentType, NotificationDAO notificationDAO,
            SimpMessagingTemplate wsTemplate) {

        try {

            Notification notification = new Notification();
            notification.setMessage(message);
            notification.setEntityID(entityID);
            notification.setWhen(new Date());
            notification.setUser(user);
            notification.setTimestamp(notification.getWhen().getTime());
            notification.setRedirectURL(redirectURL);
            notification.setComponentType(componentType);
            notification.setClasses(clazz);
            notification.setNotificationType(Notification.NotificationType.PUSH.getFriendlyName());

            notificationDAO.save(notification);

            notification.setWhen(null);

            String notificationAsString = objectMapper.writeValueAsString(notification);

            wsTemplate.convertAndSend(NOTIFICATION_TOPIC, notificationAsString);
//        wsTemplate.convertAndSendToUser(user.getUsername(), NOTIFICATION_TOPIC, notificationAsString);

            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return false;
    }

    public static String serializeHttpServletRequestHeaders(HttpServletRequest request) {

        List<String> headers = new ArrayList();

        try {

            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                headers.add(headerName + ":" + headerValue);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }

        return new Gson().toJson(headers);

    }

    public static boolean validateXMLSchema(String xsdFilename, String xmlPath) {

        ClassLoader classLoader = Util.class.getClass().getClassLoader();

        String xsdSchemaFile = classLoader.getResource(xsdFilename).getFile();

        logger.info("Path: " + xsdSchemaFile);

        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdSchemaFile));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new File(xmlPath)));
        } catch (IOException | SAXException e) {
            e.printStackTrace();
            logger.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
        return true;
    }

    private static String calculateHexString() {
        int length = 10;
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(Integer.toHexString(random.nextInt()));
        }
        return sb.substring(0, 10);
    }

    public static String createRandomHEXString() {

        String hexID = eu.orchestrator.transfer.util.Util
                .generateRandomString(10, eu.orchestrator.transfer.util.Util.Mode.ALPHANUMERIC_IGNORE_CASE);
        return hexID;

        /*String hexID = null;

        // TODO Check uniqueness
        Query query = entityManager.createNativeQuery("select hex_id from hex where hex_id = ?;");

        boolean hexOK = false;

        do {

            try {

                hexID = eu.orchestrator.transfer.service.Util.generateRandomString(10, eu.orchestrator.transfer.service.Util.Mode.ALPHANUMERIC);

                query.setParameter(1, hexID);

                if (query.getSingleResult() == null) {
                    hexOK = true;
                }

            } catch (NoResultException nre) {
                hexOK= true;
            }

        } while (!hexOK);

        if (null != hexID && !hexID.isEmpty()) {

            query = entityManager.createNativeQuery("insert into hex (hex_id) values (?);");
            query.setParameter(1, hexID);
            query.executeUpdate();

            return  hexID;

        }

        return null;*/

    }

    public static Page<Object> convertToTO(Class toClass, Page<Object> page,
            Pageable pageable) {

        try {

            if (null != page && null != page.getContent() && !page.getContent().isEmpty()) {

                List<Object> listTOs = new ArrayList<>();

                page.getContent().forEach(obj -> {

                    try {

                        Class copyObject = mapper.readValue(mapper.writeValueAsString(obj), toClass.getClass());

                        listTOs.add(copyObject);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

                if (!listTOs.isEmpty()) {
                    new PageImpl<>(listTOs, pageable, page.getTotalElements());
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return page;
    }

    public static String encrypt(String strToEncrypt, String myKey) {
        strToEncrypt = (null == strToEncrypt ? "" : strToEncrypt);
        MessageDigest sha = null;
        String encryptedString = null;
        try {
            key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            encryptedString = new String(Base64.encodeBase64Chunked(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8))));

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
            key = myKey.getBytes(StandardCharsets.UTF_8);
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

    public static String stringHash(String inputString) {
        inputString = (null == inputString ? "" : inputString);

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        messageDigest.update(inputString.getBytes());
        String hashingString = new String(messageDigest.digest());

        return null == hashingString ? "" : hashingString;
    }


    public static <T> String toJson(T obj) {
        return new GsonBuilder().disableHtmlEscaping().create().toJson(obj);
    }
}
