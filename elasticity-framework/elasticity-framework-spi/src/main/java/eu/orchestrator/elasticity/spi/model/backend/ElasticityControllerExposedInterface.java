package eu.orchestrator.elasticity.spi.model.backend;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 10/7/2019
 */
public class ElasticityControllerExposedInterface {

    private String name;
    private String port;
    private String vna;
    private String interfaceType;
    private String transmissionProtocol;

    public ElasticityControllerExposedInterface() {
        this.vna = "VNA0";
    }

    public enum InterfaceType {

        CORE("Core"),
        ACCESS("Access");

        private String friendlyName;

        InterfaceType(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public enum TransmissionProtocol {

        TCP("TCP"),
        UDP("UDP"),
        BOTH("Both");

        private String friendlyName;

        TransmissionProtocol(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        public String getFriendlyName() {
            return friendlyName;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getVna() {
        return vna;
    }

    public void setVna(String vna) {
        this.vna = vna;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getTransmissionProtocol() {
        return transmissionProtocol;
    }

    public void setTransmissionProtocol(String transmissionProtocol) {
        this.transmissionProtocol = transmissionProtocol;
    }
}
