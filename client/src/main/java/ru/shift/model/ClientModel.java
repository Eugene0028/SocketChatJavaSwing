package ru.shift.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import ru.shift.Message;
import ru.shift.protocol.ResponseStatus;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Log4j2
public class ClientModel {
    private ChatClient client;
    private final List<ChatEventListener> chatEventListeners = new ArrayList<>();

    public ClientModel()  {}

    public void tryToConnect(String ip, String port, String username) {
        try {
            int parsedPort = Integer.parseInt(port);
            Socket socket = new Socket(ip, parsedPort);

            client = new ChatClient(this, socket);

            client.sendLoginRequest(username);
            client.startListening();
        }

        catch (NumberFormatException e) {
            notifyLoginChatEvent(ResponseStatus.FAILED);
            log.error("Некорректный формат порта: '{}'", port, e);
        } catch (IOException e) {
            notifyLoginChatEvent(ResponseStatus.FAILED);
            log.error("Ошибка подключения к серверу {}:{}", ip, port);
        } catch (Exception e) {
            notifyLoginChatEvent(ResponseStatus.FAILED);
            log.error("Непредвиденная ошибка при подключении", e);
        }
    }

    public void tryToSendMessage(String text) {
        try {
            client.sendMessage(text);
        } catch (JsonProcessingException e) {
            log.error("Exception when I want to send message", e);
        }
    }


    public void notifyLoginChatEvent(ResponseStatus responseStatus) {
        chatEventListeners.forEach(x -> x.onLoginResponse(responseStatus));
    }

    public void notifyErrorChatEvent() {
        chatEventListeners.forEach(ChatEventListener::onError);
    }

    public void notifyMessageChatEvent(Message message) {
        chatEventListeners.forEach(x -> x.onMessageReceived(message));
    }

    public void notifyOnlineUsersChatEvent(Set<String> usernames) {
        chatEventListeners.forEach(x -> x.onUpdateUsernames(usernames));
    }

    public void addChatEventListener(ChatEventListener listener) {
        chatEventListeners.add(listener);
    }
}
