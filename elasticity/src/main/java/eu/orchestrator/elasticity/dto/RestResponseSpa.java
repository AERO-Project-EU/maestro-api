package eu.orchestrator.elasticity.dto;

import java.io.Serializable;

public class RestResponseSpa<R extends Serializable> implements Serializable {


    private String code;
    private String message;
    private R returnobject;

    public RestResponseSpa(String code, String message, R returnobject) {
        this.message = message;
        this.code = code;
        this.returnobject = returnobject;
    }

    public RestResponseSpa(String code, String message) {
        this.message = message;
        this.code = code;
    }

    public RestResponseSpa() {
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
