package eu.orchestrator.transfer.entities.rainbowk8s;

import java.io.Serializable;

public class ImagePullSecret implements Serializable {

    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public static class Builder {
        private String name;

        public ImagePullSecret.Builder withName(String name) {
            this.name = name;
            return this;
        }

        public ImagePullSecret build() {
            ImagePullSecret imagePullSecret = new ImagePullSecret();

            imagePullSecret.setName(this.name);

            return imagePullSecret;
        }
    }
}
