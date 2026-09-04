package eu.orchestrator.elasticity.spi.service.orchestrator;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 6/8/2019
 */
public class MetadataConverter<TO> implements Serializable {
    private Class className;

    public MetadataConverter() {
    }

    public TO convertMetadata(Object metadataObject, Class<TO> classTO){
        Gson gson = new Gson();
        String json = gson.toJson(metadataObject); // May not serialize foo.value correctly
        TO convertedMetadata = gson.fromJson(json, classTO);
        return convertedMetadata;
    }
}
