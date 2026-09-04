package eu.orchestrator.backend.transfer;

import java.io.Serializable;
import java.util.List;

/**
 * @deprecated
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 29/1/21
 */
public class ApplicationInstanceIpTO implements Serializable {

    private Long id;
    private String name;
    private List<String> ipList;

    public ApplicationInstanceIpTO() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }
}
