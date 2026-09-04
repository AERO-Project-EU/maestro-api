package eu.orchestator.core.util;

import eu.orchestator.core.configuration.ConsulConfig;
import eu.orchestrator.elasticity.spi.model.orchestrator.ConsulConfigTO;

/**
 * @author Konstantinos Theodosiou.
 * @email konstheodosiou@gmail.com
 * @date 26/7/2019
 */
public class ConverterTo {

    public static ConsulConfigTO consulConfigToConverter(ConsulConfig consulConfig) {
        ConsulConfigTO consulConfigTo = new ConsulConfigTO();
        consulConfigTo.setUrl(consulConfig.getUrl());
        consulConfigTo.setPort(consulConfig.getPort());
        consulConfigTo.setIpv6(consulConfig.getIpv6());
        consulConfigTo.setIpv6Enabled(consulConfig.getIpv6Enabled());

        return consulConfigTo;
    }
}
