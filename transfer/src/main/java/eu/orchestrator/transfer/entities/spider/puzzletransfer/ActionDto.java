package eu.orchestrator.transfer.entities.spider.puzzletransfer;

import java.io.Serializable;


public class ActionDto implements Serializable {

    private String method;

    private String endpoint;

    private String body;

    
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
