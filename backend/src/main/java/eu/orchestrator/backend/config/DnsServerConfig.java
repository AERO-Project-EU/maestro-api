package eu.orchestrator.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 17/12/19
 */

@Configuration
public class DnsServerConfig {

    @Value("${dns.url}")
    private String url;

    @Value("${dns.credentials.username}")
    private String username;

    @Value("${dns.credentials.password}")
    private String password;

    @Value("${dns.domains}")
    private String[] dnsDomains;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String[] getDnsDomains() {
        return dnsDomains;
    }

    public void setDnsDomains(String[] dnsDomains) {
        this.dnsDomains = dnsDomains;
    }
}
