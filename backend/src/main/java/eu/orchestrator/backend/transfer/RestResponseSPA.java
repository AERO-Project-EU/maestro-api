package eu.orchestrator.backend.transfer;

import java.io.Serializable;

public class RestResponseSPA<R extends Serializable> implements Serializable {


    private String code;
    private String message;
    private R returnobject;

    public RestResponseSPA(String code, String message, R returnobject) {
        this.message = message;
        this.code = code;
        this.returnobject = returnobject;
    }

    public RestResponseSPA(String code, String message) {
        this.message = message;
        this.code = code;
    }

    public RestResponseSPA() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public R getReturnobject() {
        return returnobject;
    }

    public void setReturnobject(R returnobject) {
        this.returnobject = returnobject;
    }
}
