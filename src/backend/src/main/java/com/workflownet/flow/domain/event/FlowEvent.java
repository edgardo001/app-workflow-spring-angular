package com.workflownet.flow.domain.event;

import java.time.Instant;

public interface FlowEvent {

    String getEventId();

    String getFlowId();

    Instant getTimestamp();
}
