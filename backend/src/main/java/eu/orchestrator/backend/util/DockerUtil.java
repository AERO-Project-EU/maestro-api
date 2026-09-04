package eu.orchestrator.backend.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

public class DockerUtil {

    public static Boolean dockerCredentialsValidator(String username, String password,
            String registry) {

        try {
            DefaultDockerClientConfig config
                    = DefaultDockerClientConfig.createDefaultConfigBuilder()
                    .withRegistryUsername(username)
                    .withRegistryPassword(password)
                    .withRegistryUrl(registry).build();

            DockerClient dockerClient = DockerClientBuilder.getInstance(config).build();

            dockerClient.authCmd().exec();
            return true;

        } catch (Exception e) {

            return false;
        }
    }
}
