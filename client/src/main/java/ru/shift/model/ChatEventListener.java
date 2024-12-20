package ru.shift.model;

import ru.shift.Message;
import ru.shift.protocol.LoginResponse;
import ru.shift.protocol.ResponseStatus;

import java.util.Set;

public interface ChatEventListener {
    void onLoginResponse(ResponseStatus status);
    void onMessageReceived(Message message);
    void onError();
    void onUpdateUsernames(Set<String> usernames);
}