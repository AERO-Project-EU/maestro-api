package eu.orchestrator.backend.util;

import eu.orchestrator.transfer.entities.dnsServer.CredentialsTO;
import eu.orchestrator.transfer.entities.dnsServer.DNSEntryTO;
import eu.orchestrator.backend.config.DnsServerConfig;
import eu.orchestrator.common.enums.GenericMessage;
import eu.orchestrator.backend.transfer.RestResponseSPA;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 17/12/19
 */
@SuppressWarnings("Duplicates")
public class DNSUtil {

    public static RestResponseSPA addDomain(DnsServerConfig dnsServerConfig, String subDomain,
            String domain,
            String machineIP) {

        String dnsServerURI = "http://" + dnsServerConfig.getUrl() + "/api/v1/domain/add";

        CredentialsTO credentialsTO = new CredentialsTO();
        credentialsTO.setUsername(dnsServerConfig.getUsername());
        credentialsTO.setPassword(dnsServerConfig.getPassword());

        DNSEntryTO dnsEntryTO = new DNSEntryTO();
        dnsEntryTO.setIp(machineIP);
        dnsEntryTO.setSubDomain(subDomain);
        dnsEntryTO.setFullDomain(subDomain + "." + domain);
        dnsEntryTO.setCredentials(credentialsTO);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(dnsEntryTO);

        try {
            ResponseEntity<RestResponseSPA> responseEntity = restTemplate
                    .exchange(dnsServerURI, HttpMethod.POST, entity, RestResponseSPA.class);
            RestResponseSPA<DNSEntryTO> restResponseSPA = responseEntity.getBody();
            return restResponseSPA;
        } catch (final HttpClientErrorException httpClientErrorException) {

            return new RestResponseSPA(GenericMessage.DOMAIN_ALREADY_EXIST.getCode(),
                    GenericMessage.DOMAIN_ALREADY_EXIST.getMessageEN());

        } catch (HttpServerErrorException httpServerErrorException) {

            return new RestResponseSPA(GenericMessage.DOMAIN_SERVICE_CREATION_FAILED.getCode(),
                    GenericMessage.DOMAIN_SERVICE_CREATION_FAILED.getMessageEN());

        } catch (Exception exception) {

            return new RestResponseSPA(GenericMessage.DOMAIN_SERVICE_CREATION_FAILED.getCode(),
                    GenericMessage.DOMAIN_SERVICE_CREATION_FAILED.getMessageEN());
        }
    }

    public static RestResponseSPA updateDomain(DnsServerConfig dnsServerConfig, String subDomain,
            String domain, String machineIP) {

        String dnsServerURI = "http://" + dnsServerConfig.getUrl() + "/api/v1/domain/update";

        CredentialsTO credentialsTO = new CredentialsTO();
        credentialsTO.setUsername(dnsServerConfig.getUsername());
        credentialsTO.setPassword(dnsServerConfig.getPassword());

        DNSEntryTO dnsEntryTO = new DNSEntryTO();
        dnsEntryTO.setIp(machineIP);
        dnsEntryTO.setSubDomain(subDomain);
        dnsEntryTO.setFullDomain(subDomain + "." + domain);
        dnsEntryTO.setCredentials(credentialsTO);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(dnsEntryTO);

        try {
            ResponseEntity<RestResponseSPA> responseEntity = restTemplate
                    .exchange(dnsServerURI, HttpMethod.POST, entity, RestResponseSPA.class);
            RestResponseSPA<DNSEntryTO> restResponseSPA = responseEntity.getBody();
            return restResponseSPA;
        } catch (final HttpClientErrorException httpClientErrorException) {

            return new RestResponseSPA(GenericMessage.DOMAIN_ALREADY_EXIST.getCode(),
                    GenericMessage.DOMAIN_ALREADY_EXIST.getMessageEN());

        } catch (HttpServerErrorException httpServerErrorException) {

            return new RestResponseSPA(GenericMessage.DOMAIN_SERVICE_CREATION_FAILED.getCode(),
                    GenericMessage.DOMAIN_SERVICE_CREATION_FAILED.getMessageEN());

        } catch (Exception exception) {

            return new RestResponseSPA(GenericMessage.DOMAIN_SERVICE_CREATION_FAILED.getCode(),
                    GenericMessage.DOMAIN_SERVICE_CREATION_FAILED.getMessageEN());
        }
    }

    public static RestResponseSPA deleteDomain(DnsServerConfig dnsServerConfig, String domain) {

        String dnsServerURI = "http://" + dnsServerConfig.getUrl() + "/api/v1/domain/delete";

        CredentialsTO credentialsTO = new CredentialsTO();
        credentialsTO.setUsername(dnsServerConfig.getUsername());
        credentialsTO.setPassword(dnsServerConfig.getPassword());

        DNSEntryTO dnsEntryTO = new DNSEntryTO();
        dnsEntryTO.setFullDomain(domain);
        dnsEntryTO.setCredentials(credentialsTO);

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity entity = new HttpEntity(dnsEntryTO);

        try {
            ResponseEntity<RestResponseSPA> responseEntity = restTemplate
                    .exchange(dnsServerURI, HttpMethod.POST, entity, RestResponseSPA.class);
            RestResponseSPA<DNSEntryTO> restResponseSPA = responseEntity.getBody();
            return restResponseSPA;
        } catch (final HttpClientErrorException httpClientErrorException) {

            ObjectMapper objectMapper = new ObjectMapper();

            try {
                RestResponseSPA<DNSEntryTO> restResponseSPA = objectMapper.readValue(httpClientErrorException.getResponseBodyAsString(), RestResponseSPA.class);

                return restResponseSPA;

            } catch (IOException e) {

                return new RestResponseSPA(GenericMessage.DOMAIN_SERVICE_DELETION_FAILED.getCode(),
                        GenericMessage.DOMAIN_SERVICE_DELETION_FAILED.getMessageEN());
            }

        } catch (HttpServerErrorException httpServerErrorException) {

            return new RestResponseSPA(GenericMessage.DOMAIN_SERVICE_DELETION_FAILED.getCode(),
                    GenericMessage.DOMAIN_SERVICE_DELETION_FAILED.getMessageEN());

        } catch (Exception exception) {

            return new RestResponseSPA(GenericMessage.DOMAIN_SERVICE_DELETION_FAILED.getCode(),
                    GenericMessage.DOMAIN_SERVICE_DELETION_FAILED.getMessageEN());
        }
    }

    public static boolean urlValidator(String url) {

        try {
            InetAddress inetAddress = InetAddress.getByName(url);
            return true;
        } catch (UnknownHostException e) {
            return false;
        }

    }

}
