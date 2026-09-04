package eu.orchestrator.transfer.response;

import java.io.Serializable;

public class FeignResponse<R> implements Serializable {

    private String code;
    private String message;
    private R returnobject;

    public FeignResponse(String code, String message, R returnobject) {
        this.message = message;
        this.returnobject = returnobject;
        this.code = code;
    }

    public FeignResponse(String code, String message) {
        this.message = message;
        this.code = code;
    }

    public FeignResponse() {
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