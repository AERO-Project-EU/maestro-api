package eu.orchestrator.backend.security;

import eu.orchestrator.backend.util.Util;

import com.google.gson.GsonBuilder;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import jakarta.servlet.http.HttpServletRequest;

public class JWTSecurityHandler {

    public static String createSignedToken(String sginerSecret, JWTClaimsSet jwtClaims)
            throws JOSEException {

        //Create a new signer
        JWSSigner signer = new MACSigner(sginerSecret);

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaims);

        // Sign
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    public static String createEncryptedToken(String sginerSecret, RSAPublicKey publicKey,
            JWTClaimsSet jwtClaims) throws JOSEException {

        //Create a new signer
        JWSSigner signer = new MACSigner(sginerSecret);

        // Request JWT encrypted with RSA-OAEP and 128-bit AES/GCM
        JWEHeader header = new JWEHeader(JWEAlgorithm.RSA_OAEP, EncryptionMethod.A128GCM);

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), jwtClaims);

        // Sign
        signedJWT.sign(signer);

        JWEObject jweObject = new JWEObject(header, new Payload(signedJWT));

        return encryptedToken(jweObject, publicKey).serialize();

    }

    private static JWEObject encryptedToken(JWEObject jwt, RSAPublicKey publicKey)
            throws JOSEException {
        // Create an encrypter with the specified public RSA key
        RSAEncrypter encrypter = new RSAEncrypter(publicKey);
        jwt.encrypt(encrypter);
        return jwt;
    }

    /**
     * @param jwtToken   The encrypted JWT
     * @param privateKey An RSAPrivateKey which is used to decrypt an encrypted JWT
     */
    public static SignedJWT parseEncryptedJWT(String jwtToken, String signerKey,
            RSAPrivateKey privateKey)
            throws ParseException, JOSEException, SignatureNotVerifiedException {
        JWEObject jweObject = JWEObject.parse(jwtToken);
        // Create a decrypter with the specified private RSA key
        RSADecrypter decrypter = new RSADecrypter(privateKey);
        // Decrypt
        jweObject.decrypt(decrypter);
        // Extract payload
        SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
        //Verify
        boolean isVerified = signedJWT.verify(new MACVerifier(signerKey));
        if (isVerified) {
            return signedJWT;
        }
        throw new SignatureNotVerifiedException("Could not verify signature of the token");

    }

    public static SignedJWT parseSignedJWT(String jwtToken, String signerKey,
            HttpServletRequest request)
            throws ParseException, JOSEException, SignatureNotVerifiedException {

        // Extract payload
        SignedJWT signedJWT = SignedJWT.parse(jwtToken);

        //Verify
        boolean isVerified = signedJWT.verify(new MACVerifier(signerKey));
        if (isVerified) {
            return signedJWT;
        }

        throw new SignatureNotVerifiedException(
                "Could not verify signature of the token from request: " + new GsonBuilder()
                        .setPrettyPrinting().create().toJson(Util.serializeHttpServletRequestHeaders(request)));

    }

}
