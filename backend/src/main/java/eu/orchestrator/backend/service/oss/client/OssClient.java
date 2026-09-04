package eu.orchestrator.backend.service.oss.client;

import eu.orchestrator.transfer.entities.oss.SliceIntent;

public interface OssClient {
    boolean requestSlice(SliceIntent sliceIntent);
}
