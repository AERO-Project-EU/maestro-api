package eu.orchestator.core.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Konstantinos Theodosiou.
 */
@EnableConfigurationProperties
@Component
public class UbiDellConfig {

    @Value("${ubidell.image.id}")
    String imageId;

    @Value("${ubidell.flavor.id}")
    String flavorId;

    @Value("${ubidell.network.id}")
    String networkId;

    @Value("${ubidell.key.id}")
    String keyId;

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getFlavorId() {
        return flavorId;
    }

    public void setFlavorId(String flavorId) {
        this.flavorId = flavorId;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }
}