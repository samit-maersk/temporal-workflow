package com.example.temporalworkflow.workflows;

import com.example.temporalworkflow.model.User;
import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface UserActivity {
    void notification(User user);

    void persist(User user);
}
