package org.example.telegram.handler;

import org.example.telegram.handler.enums.UserState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStateStorage {
    private final Map<Long, UserState> states = new ConcurrentHashMap<>();

    public UserState getState(long tgUserId) {
        return states.getOrDefault(tgUserId, UserState.NONE);
    }

    public void setState(long tgUserId, UserState state) {
        states.put(tgUserId, state);
    }

    public void clearState(long tgUserId) {
        states.remove(tgUserId);
    }
}
