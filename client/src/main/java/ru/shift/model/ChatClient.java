package ru.shift.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.log4j.Log4j2;
import ru.shift.JsonUtil;
import ru.shift.Message;
import ru.shift.protocol.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;

@Log4j2
public class ChatClient implements AutoCloseable {
    private final ClientModel model;
    private final Socket socket;
    private final PrintWriter out;
    private final BufferedReader in;
    private String username;

    public ChatClient(ClientModel model, Socket socket) throws IOException {
        this.model = model;
        this.socket = socket;
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendLoginRequest(String username) throws JsonProcessingException {
        this.username = username;
        LoginRequest request = new LoginRequest(username);
        sendRequest(request);
    }

    public void sendMessage(String content) throws JsonProcessingException {
        Message message = new Message(username, content, new Timestamp(System.currentTimeMillis()));
        MessageRequest request = new MessageRequest(message);
        sendRequest(request);
    }

    private void sendRequest(Request request) throws JsonProcessingException {
        String json = JsonUtil.toJson(request);
        out.println(json);
    }

    public void startListening() {
        new Thread(() -> {
            try {
                String response;
                while ((response = in.readLine()) != null) {
                    processResponse(response);
                }
            } catch (IOException e) {
                log.error("Server probably die. Back to connect window.");
                model.notifyErrorChatEvent();
            }
        }).start();
    }

    private void processResponse(String responseJson)  {
        try {
            Response response = JsonUtil.fromJson(responseJson, Response.class);
            if (response instanceof LoginResponse loginResponse) {
                log.info("Client get Login Response!");
                model.notifyLoginChatEvent(loginResponse.getResponseStatus());
            }
            else if (response instanceof MessageResponse messageResponse) {
                log.info("Client get Message Response!");
                model.notifyMessageChatEvent(messageResponse.getMessage());
            }
            else if (response instanceof OnlineUsersUpdate onlineUsersUpdate) {
                log.info("Count of users changed!");
                model.notifyOnlineUsersChatEvent(onlineUsersUpdate.getUsernames());
            }
        } catch (JsonProcessingException e) {
            log.error("Exception when I try to get response from JSON");
        }
    }

    @Override
    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            log.error("Error when on close socket");
        }
    }
}
