package com.example.temporalworkflow.workflows;

import com.example.temporalworkflow.model.User;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserActivityImpl implements UserActivity {
    @Override
    public void notification(User user) {
        log.info("Sending notification to {}", user);
    }

    @Override
    public void persist(User user) {
        log.info("Persisting user {}", user);
    }
}
