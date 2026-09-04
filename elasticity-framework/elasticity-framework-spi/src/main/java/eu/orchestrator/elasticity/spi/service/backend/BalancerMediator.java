package eu.orchestrator.elasticity.spi.service.backend;

import org.springframework.beans.factory.annotation.Value;

import jakarta.transaction.Transactional;

@org.springframework.stereotype.Service
@Transactional
public class BalancerMediator {

    @Value("${consul.server.url}")
    String consulURLIPv4;

    @Value("${consul.server.ipv6}")
    String consulURLIPv6;

    public String getConsulIPv4URL(){
        return consulURLIPv4;
    }

    public String getConsulIPv6URL(){
        return consulURLIPv6;
    }

    public void getConsulIPv4(){
    }

    public void getConsulIPv6(){
    }

    public void getConsulIPv4Port(){
    }

    public void getConsulIPv6Port(){
    }


    public void getKafkaIPv4(){

    }

    public void getKafkaIPv6(){

    }

    public void getKafkaPort(){

    }

    public void getKafkaTopic(){

    }

    public void getKafkaGroup(){

    }
}
