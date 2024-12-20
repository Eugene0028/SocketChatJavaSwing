package ru.shift.controller;

import ru.shift.Message;
import ru.shift.model.ChatEventListener;
import ru.shift.model.ClientModel;
import ru.shift.protocol.ResponseStatus;
import ru.shift.view.ChatClientGUI;
import ru.shift.view.ConnectButtonEventListener;
import ru.shift.view.SendMessageEventListener;

import java.util.Set;

public class ViewController implements ConnectButtonEventListener, ChatEventListener, SendMessageEventListener {
    private final ClientModel model;
    private final ChatClientGUI view;

    public ViewController(ClientModel model, ChatClientGUI view) {
        this.model = model;

        this.view = view;
        this.view.setButtonListener(this);
        this.view.setMessageEventListener(this);

        this.model.addChatEventListener(this);

    }

    @Override
    public void connectEvent(String ip, String port, String username) {
        model.tryToConnect(ip, port, username);
    }

    @Override
    public void sendMessageEvent(String text) {
        model.tryToSendMessage(text);
    }

    @Override
    public void onLoginResponse(ResponseStatus status) {
        view.visualiseConnectStatus(status);
    }

    @Override
    public void onMessageReceived(Message message) {
        view.visualiseNewMessage(message);
    }

    @Override
    public void onError() {
        view.initConnectWindow();
    }

    @Override
    public void onUpdateUsernames(Set<String> usernames) {
        view.updateUserNamesWindow(usernames);
    }
}

