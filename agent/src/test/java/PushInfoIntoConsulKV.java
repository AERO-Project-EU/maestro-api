
/**
 * @author Panagiotis Parthenis.
 */
public class PushInfoIntoConsulKV {

    public static void main(String[] args) {

        //  graphVI graphInstanceId componentNodeId6 componentNodeInstanceIdA true

      /*  ConsulClient consulClient = new ConsulClient(System.getProperty("consul.host", "localhost"));
        ObjectMapper objectMapper = new ObjectMapper();

        HealthCheck healthCheck = new HealthCheck();
        healthCheck.setInterval("10s");
        healthCheck.setHttpURL("http://localhost:15568/metrics");
        healthCheck.setArgs("");

        String jsonInStringImage = "";
        try {
            jsonInStringImage = objectMapper.writeValueAsString(healthCheck);
        } catch (JsonProcessingException exception) {
            exception.printStackTrace();
        }

        consulClient.setKVValue("LambdaApp/traefik/LambdaProxy/configurations/healthcheck", jsonInStringImage);*/



    }
}