package eu.orchestrator.backend.transfer;

import java.util.Date;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 27/1/21
 */
public class UserTokenTO {

    private String name;
    private String username;
    private String token;
    private Date expirationDate;
    private Long id;

    public UserTokenTO() {

    }

    public UserTokenTO(String name, String username, String token, Date expirationDate, Long id) {
        this.name = name;
        this.username = username;
        this.token = token;
        this.expirationDate = expirationDate;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
