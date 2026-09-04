package eu.orchestator.core.model.agent;

/**
 * @author Konstantinos Theodosiou.
 * @email konstheodosiou@gmail.com
 * @date 16/12/19
 */
public enum HostsMappingStatus {
    WAIT(0),
    PROCEED(1),
    FETCH(2);

    private final int status;

    HostsMappingStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
