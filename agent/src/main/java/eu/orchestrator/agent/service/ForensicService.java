package eu.orchestrator.agent.service;

import eu.orchestrator.agent.configuration.AgentConfiguration;
import eu.orchestrator.agent.util.CommandLineExecutor;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Konstantinos Theodosiou.
 * @email konstheodosiou@gmail.com
 * @date 26/5/20
 */
public class ForensicService {

    //LOGGER
    private static final Logger LOGGER = Logger.getLogger(IntegrityService.class.getName());

    private String graphHexId;
    private String graphInstanceHexId;
    private String componentNodeHexId;
    private String componentNodeInstanceHexId;
    private String forensicServerUrl = AgentConfiguration.FORENSIC_SERVER_HOST;
    private String forensicAgentUrl = AgentConfiguration.FORENSIC_AGENT_URL;
    private String forensicAgentUsername = AgentConfiguration.FORENSIC_AGENT_USERNAME;
    private String forensicAgentPassword = AgentConfiguration.FORENSIC_AGENT_PASSWORD;

    private ConsulClient consulClient;

    public ForensicService(String graphHexId, String graphInstanceHexId, String componentNodeHexId, String componentNodeInstanceHexId,
            ConsulClient consulClient) {
        this.graphHexId = graphHexId;
        this.graphInstanceHexId = graphInstanceHexId;
        this.componentNodeHexId = componentNodeHexId;
        this.componentNodeInstanceHexId = componentNodeInstanceHexId;
        this.consulClient = consulClient;
    }

    public void run() {

        LOGGER.log(Level.INFO, "RUN");
        if (getForensicConsulStatus() == 0) {
            LOGGER.log(Level.INFO, " Forensic Consul Status 0 ");

            if (!checkInstalled()) {
                LOGGER.log(Level.INFO, "Let's install Forensic Agent");
                installForensicAgent();
            }
            if (!status()) {
                LOGGER.log(Level.INFO, " Enable");
                enable();
            }
            setForensicConsulStatus(1);
        } else {
            LOGGER.log(Level.INFO, " Forensic Consul Status 1");
            if (status()) {
                LOGGER.log(Level.INFO, " Disable");
                disable();
            }
            setForensicConsulStatus(0);
        }
    }

    private boolean enable() {
        String[] cmd = {
                "/bin/sh",
                "-c",
                "sudo service owlhstap start"
        };
        String output = CommandLineExecutor.multiLine(cmd);

        return true;
    }

    public boolean disable() {
        String[] cmd = {
                "/bin/sh",
                "-c",
                "sudo service owlhstap stop"
        };
        String output = CommandLineExecutor.multiLine(cmd);
        return true;
    }

    private boolean status() {

        String[] cmd = {
                "/bin/sh",
                "-c",
                "sudo service owlhstap status | grep inactive"
        };
        String output = CommandLineExecutor.multiLine(cmd);

        // Check if it is inactive return false
        if (output == null || output.contains("inactive")) {
            return false;
        }
        return true;
    }

    private boolean checkInstalled() {

        String[] cmd = {
                "/bin/sh",
                "-c",
                "sudo service owlhstap status | grep \"could not be found\""
        };
        String output = CommandLineExecutor.multiLine(cmd);

        // Check if it is installed return true
        if (output == null || output.contains("could not be found")) {
            return false;
        }
        return true;
    }

    public void installForensicAgent() {

        if (forensicServerUrl.isEmpty()) {
            LOGGER.log(Level.WARNING,
                    "Forensic server host is not configured (AGENT_FORENSIC_SERVER_HOST), skipping installation");
            return;
        }

        String forensicAgentFetchUrl = "sudo wget --user=" + forensicAgentUsername
                + " --password=" + forensicAgentPassword + " " + forensicAgentUrl + " -O /opt/owlhclient.sh";

        //        String[] cmd = {
        //                "/bin/sh",
        //                "-c",
        //                forensicAgentFetchUrl  + " && " + "chmod +x /opt/owlhclient.sh"  + " && " + "/opt/owlhclient.sh"
        //        };

        String[] cmd = {
                "/bin/sh",
                "-c",
                "sed -i -e 's/1.1.1.1/" + forensicServerUrl + "/g' /usr/local/owlh/bin/conf.json " + " && "
                        + "update-rc.d  owlhstap enable"
        };
        String output = CommandLineExecutor.multiLine(cmd);
        LOGGER.log(Level.INFO, "Output " + output);
    }

    public int getForensicConsulStatus() {
        int status = 0;

        Response<GetValue> kvResponse = consulClient.getKVValue(graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/"
                + componentNodeInstanceHexId + "/forensicStatus");

        //Check if the status path exists
        if (kvResponse == null || kvResponse.getValue() == null) {
            LOGGER.log(Level.INFO, "No Forensic Status value on the consul, so it is the first configuration");
            return status;
        }

        //check the value of the status
        String value = kvResponse.getValue().getDecodedValue();
        if (value == null || value.isEmpty()) {
            LOGGER.log(Level.INFO, "No Forensic Status value on the consul, so it is the first configuration");
            return status;
        }

        status = Integer.parseInt(value);
        LOGGER.log(Level.INFO, "Forensic Status value on the consul is: " + status);
        return status;
    }

    public void setForensicConsulStatus(int status) {
        consulClient.setKVValue(graphHexId + "/" + graphInstanceHexId + "/" + componentNodeHexId + "/"
                + componentNodeInstanceHexId + "/forensicStatus", "" + status + "");
        LOGGER.log(Level.INFO, "Forensic Status --------------------------> {0}", status);
    }
}
