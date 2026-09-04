package eu.orchestrator.metric.exporter.service;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ConsulService {

    private final ConsulClient client;

    @Autowired
    public ConsulService(ConsulClient client) {
        this.client = client;
    }

    public Response<List<HealthService>> getHealthServices(String serviceName) {
        return client.getHealthServices(serviceName, true, QueryParams.DEFAULT);
    }

    public HealthService getService(String componentPatternPrefix) {
        Response<List<HealthService>> healthyServices = getHealthServices("netdata");

        Optional<HealthService> serviceOptional = healthyServices.getValue()
                .stream()
                .filter(service -> service.getNode().getNode().startsWith(componentPatternPrefix))
                .findFirst();

        if (!serviceOptional.isPresent()) {
            throw new RuntimeException("Requested service does not exist in consul");
        }

        return serviceOptional.get();
    }

}
