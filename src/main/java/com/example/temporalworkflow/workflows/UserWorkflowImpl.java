package com.example.temporalworkflow.workflows;

import com.example.temporalworkflow.model.Status;
import com.example.temporalworkflow.model.User;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class UserWorkflowImpl implements UserWorkflow {

    RetryOptions retryOptions = RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofSeconds(1))
            .setMaximumInterval(Duration.ofSeconds(100))
            .setBackoffCoefficient(2)
            .setMaximumAttempts(2)
            .build();
    ActivityOptions options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(10))
            .setRetryOptions(RetryOptions.newBuilder()
                    .setInitialInterval(Duration.ofSeconds(1))
                    .setMaximumInterval(Duration.ofSeconds(100))
                    .setBackoffCoefficient(2)
                    .setMaximumAttempts(500)
                    .build())
            .build();
    UserActivity userActivity = Workflow.newActivityStub(UserActivity.class, options);

    private User user;

    @Override
    public void process(User u) {
        this.user = new User(u.name(), u.email(), u.message(), Status.PENDING);
        log.info("Processing user {}", this.user);
        // This workflow will run for 5 minutes or until the user is APPROVED or REJECTED
        Workflow.await(Duration.ofMinutes(5), () -> this.user.status() == Status.APPROVED || this.user.status() == Status.REJECTED);

        if (this.user.status() == Status.APPROVED) {
            log.info("User {} is approved", this.user);
            userActivity.persist(this.user);
        } else {
            log.info("User {} is rejected", this.user);
        }

        userActivity.notification(this.user);
    }

    @Override
    public void approved() {
        log.info("APPROVED signal received");
        this.user = new User(user.name(), user.email(), user.message(), Status.APPROVED);
    }

    @Override
    public void rejected() {
        log.info("REJECT signal received");
        this.user = new User(user.name(), user.email(), user.message(), Status.REJECTED);
    }

    @Override
    public User getStatus() {
        return this.user;
    }
}
