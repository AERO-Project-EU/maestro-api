package eu.orchestator.core.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author Konstantinos Theodosiou.
 */
@EnableConfigurationProperties
@Component
public class AmazonConfig {

    @Value("${amazon.image.id}")
    String imageId;

    @Value("${amazon.flavor.id}")
    String flavorId;

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
}

