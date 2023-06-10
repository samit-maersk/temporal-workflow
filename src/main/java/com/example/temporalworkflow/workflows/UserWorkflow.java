package com.example.temporalworkflow.workflows;

import com.example.temporalworkflow.model.User;
import io.temporal.workflow.QueryMethod;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

import java.util.List;

@WorkflowInterface
public interface UserWorkflow {
    @WorkflowMethod
    void process(User user);

    @SignalMethod
    void approved();

    @SignalMethod
    void rejected();

    @QueryMethod
    User getStatus();
}
