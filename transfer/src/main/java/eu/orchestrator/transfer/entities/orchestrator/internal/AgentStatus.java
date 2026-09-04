package eu.orchestrator.transfer.entities.orchestrator.internal;

/**
 * @author Konstantinos Theodosiou
 * @email konstheodosiou@gmail.com
 * @date 19/7/2019
 */
public class AgentStatus {

    //Status
    private static final int SPAWNED = 1;
    private static final int STATUS_INITIALIZED = 2;
    private static final int STATUS_IMAGEDOWNLOADED = 3;
    private static final int STATUS_WAITING_FOR_DEPENDENCIES = 4;
    private static final int STATUS_TRIGGERED_CONTAINER_START = 5;
    private static final int STATUS_STARTED = 6;
    private static final int STATUS_LISTEN_FOR_COMMAND_UP = 7;
    //Status orchestrator
    private static final int SPAWNING = 0;
    private static final int STATUS_COMPONENT_INSTANCE_UP = 8;
}
