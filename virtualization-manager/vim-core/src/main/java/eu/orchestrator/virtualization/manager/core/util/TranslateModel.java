package eu.orchestrator.virtualization.manager.core.util;

import eu.orchestrator.spi.model.*;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorFlavor;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorInstance;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorProviderAuthenticationDetails;
import eu.orchestrator.transfer.entities.orchestrator.OrchestratorVolume;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Konstantinos Theodosiou
 */
@Component
public class TranslateModel {

    private static final Logger logger = Logger.getLogger(TranslateModel.class.getName());

    private static String tokenSecret;

    @Value("${token.signer.secret}")
    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public static CredentialsModel createCredentialModel(OrchestratorProviderAuthenticationDetails orchestratorProviderAuthenticationDetails) {
        CredentialsModel credentialsModel = new CredentialsModel();
        credentialsModel.setUsername(null != orchestratorProviderAuthenticationDetails.getUsername() ? orchestratorProviderAuthenticationDetails.getUsername() : null);
        credentialsModel.setPassword(null != orchestratorProviderAuthenticationDetails.getPassword() ? orchestratorProviderAuthenticationDetails.getPassword() : null);
        credentialsModel.setProject(null != orchestratorProviderAuthenticationDetails.getProject() ? orchestratorProviderAuthenticationDetails.getProject() : null);
        credentialsModel.setEndpoint(null != orchestratorProviderAuthenticationDetails.getEndpoint() ? orchestratorProviderAuthenticationDetails.getEndpoint() : null);
        credentialsModel.setDomain(null != orchestratorProviderAuthenticationDetails.getDomain() ? orchestratorProviderAuthenticationDetails.getDomain() : null);
        credentialsModel.setRegion(null != orchestratorProviderAuthenticationDetails.getRegion() ? orchestratorProviderAuthenticationDetails.getRegion() : null);
        credentialsModel.setId(null != orchestratorProviderAuthenticationDetails.getId() ? orchestratorProviderAuthenticationDetails.getId() : null);
        credentialsModel.setPrivateKey(null != orchestratorProviderAuthenticationDetails.getPrivateKey() ? orchestratorProviderAuthenticationDetails.getPrivateKey() : null);
        credentialsModel.setPublicKey(null != orchestratorProviderAuthenticationDetails.getPublicKey() ? orchestratorProviderAuthenticationDetails.getPublicKey() : null);

        if(credentialsModel.getPassword() != null){
            credentialsModel.setPassword(decrypt(credentialsModel.getPassword(),tokenSecret));
        }

        String proxyURL = null;
        int proxyPort = 0;

        if(orchestratorProviderAuthenticationDetails.getProxy()!=null &&  !orchestratorProviderAuthenticationDetails.getProxy().isEmpty()){
            String proxy = orchestratorProviderAuthenticationDetails.getProxy();
            if( ! proxy.contains(":")){
                proxyURL = proxy;
                proxyPort = 80;
            }else{
                List<String> split = Arrays.asList(proxy.split(":"));
                proxyURL = split.get(0);
                proxyPort = Integer.parseInt(split.get(1));
            }
        }

        credentialsModel.setProxyURL(proxyURL);
        credentialsModel.setProxyPort(proxyPort);

        return credentialsModel;
    }

    public static FlavorModel createFlavorModel(OrchestratorFlavor orchestratorFlavor) {
        FlavorModel flavorModel = new FlavorModel();
        flavorModel.setvCPU(null != orchestratorFlavor.getvCPUs() ? orchestratorFlavor.getvCPUs().toString() : "N/A");
        flavorModel.setRam(null != orchestratorFlavor.getRam() ? orchestratorFlavor.getRam().toString() : "N/A");
        flavorModel.setStorage(null != orchestratorFlavor.getStorage() ? orchestratorFlavor.getStorage().toString() : "N/A");
        flavorModel.setId(null != orchestratorFlavor.getId() ? orchestratorFlavor.getId() : null);
        return flavorModel;
    }

    public static ImageModel crateImageModel() {
        ImageModel imageModel = new ImageModel();
        return imageModel;
    }

    public static InstanceModel createInstanceModel(OrchestratorInstance orchestratorInstance) {
        InstanceModel instanceModel = new InstanceModel();
        instanceModel.setName(null != orchestratorInstance.getName() ? orchestratorInstance.getName() : null);
        instanceModel.setUserData(null != orchestratorInstance.getUserData() ? orchestratorInstance.getUserData() : null);
        instanceModel.setKeyPairName(null != orchestratorInstance.getKeyPair() ? orchestratorInstance.getKeyPair() : null);
        instanceModel.setNetworkIDList(null != orchestratorInstance.getNetworkIDList() ? orchestratorInstance.getNetworkIDList() : null);
        instanceModel.setId(null != orchestratorInstance.getId() ? orchestratorInstance.getId() : null);
        instanceModel.setImageID(null != orchestratorInstance.getImageID() ? orchestratorInstance.getImageID() : null);
        instanceModel.setInstanceType(null != orchestratorInstance.getInstanceType() ? orchestratorInstance.getInstanceType() : null);
        instanceModel.setFloatingIP(null != orchestratorInstance.getFloatingIP() ? orchestratorInstance.getFloatingIP() : null);
        instanceModel.setFloatingPool(null != orchestratorInstance.getFloatingPool() ? orchestratorInstance.getFloatingPool() : null);
        return instanceModel;
    }

    public static OrchestratorInstance createOrchestratorInstance(InstanceModel instanceModel) {
        OrchestratorInstance orchestratorInstance = new OrchestratorInstance();
        orchestratorInstance.setName(null != instanceModel.getName() ? instanceModel.getName() : null);
        orchestratorInstance.setUserData(null != instanceModel.getUserData() ? instanceModel.getUserData() : null);
        orchestratorInstance.setKeyPair(null != instanceModel.getKeyPairName() ? instanceModel.getKeyPairName() : null);
        orchestratorInstance.setNetworkIDList(null != instanceModel.getNetworkIDList() ? instanceModel.getNetworkIDList() : null);
        orchestratorInstance.setId(null != instanceModel.getId() ? instanceModel.getId() : null);
        return orchestratorInstance;
    }

    public static VolumeModel createVolumeModel(OrchestratorVolume orchestratorVolume){
        VolumeModel volumeModel = new VolumeModel();
        volumeModel.setName(orchestratorVolume.getName() == null ? null : orchestratorVolume.getName());
        volumeModel.setDescription(orchestratorVolume.getDescription() == null ? null : orchestratorVolume.getDescription());
        volumeModel.setId(orchestratorVolume.getId() == null ? null : orchestratorVolume.getId());
        volumeModel.setSize(orchestratorVolume.getSize() == null ? null : orchestratorVolume.getSize());
        volumeModel.setType(orchestratorVolume.getType() == null ? null : orchestratorVolume.getType());
        return volumeModel;
    }

    private static String decrypt(String strToDecrypt, String myKey) {

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
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            e.printStackTrace();
        }
        return decryptedString;
    }
}
