package ru.shift;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import ru.shift.protocol.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class ChatServer {
    private final int port;
    private final Map<String, PrintWriter> clients = new ConcurrentHashMap<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                log.info("New connection: " + clientSocket);
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            log.error("Server die");
        }
    }

    private class ClientHandler implements Runnable {
        private String username;
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @SneakyThrows
        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                while (true) {
                    String input = in.readLine();

                    if (input == null) {
                        break;
                    }

                    Request request = JsonUtil.fromJson(input, Request.class);

                    if (request instanceof LoginRequest loginRequest) {
                        log.info("New Login request! " + request);
                        handleLogin(loginRequest, out);
                    } else if (request instanceof MessageRequest messageRequest) {
                        handleMessage(messageRequest.getMessage());
                    }
                }

            } catch (IOException e) {
                log.info("Client disconnect");
            } finally {
                synchronized (clients) {
                    var out = clients.remove(username);
                    out.close();
                    updateClientsList();
                }
                log.info("Remove client from map. Available now: " + clients.size());
            }
        }


        private void handleLogin(LoginRequest loginRequest, PrintWriter out) throws JsonProcessingException {
            String username = loginRequest.getUsername();

            LoginResponse response;

            if (clients.containsKey(username)) {
                response = new LoginResponse(ResponseStatus.USERNAME_ERROR);
            } else {
                response = new LoginResponse(ResponseStatus.OK);
                this.username = username;
                clients.put(username, out);
            }

            out.println(JsonUtil.toJson(response));

            updateClientsList();
        }

        private void handleMessage(Message message) throws JsonProcessingException {
            String messageResponse = JsonUtil.toJson(new MessageResponse(message));

            var targetUsername = isPrivateMessage(message);
            if (targetUsername != null && isUserExists(targetUsername)) {
                sendPrivateMessage(targetUsername, messageResponse);
            } else {
                broadcastMessage(messageResponse);
            }
        }

        private boolean isUserExists(String targetUsername) {
            synchronized (clients) {
                return clients.containsKey(targetUsername);
            }
        }

        private String isPrivateMessage(Message message) {
            String messageContent = message.getMessage();
            if (messageContent.startsWith("@")) {
                int spaceIndex = messageContent.indexOf(' ');
                if (spaceIndex > 1) {
                    return messageContent.substring(1, spaceIndex);
                }
            }
            return null;
        }

        private void sendPrivateMessage(String targetUsername, String messageResponse) {
            synchronized (clients) {
                PrintWriter targetWriter = clients.get(targetUsername);
                PrintWriter myselfWrite = clients.get(username);
                if (targetWriter != null) {
                    if (username.equals(targetUsername)) {
                        targetWriter.println(messageResponse);
                    } else {
                        targetWriter.println(messageResponse);
                        myselfWrite.println(messageResponse);
                    }
                    log.info("Private message sent to " + targetUsername);
                }
            }
        }

        private void broadcastMessage(String messageResponse) {
            synchronized (clients) {
                clients.values().forEach(writer -> writer.println(messageResponse));
            }
        }

        private void updateClientsList() throws JsonProcessingException {
            var usernames = new OnlineUsersUpdate(clients.keySet());
            broadcastMessage(JsonUtil.toJson(usernames));
        }
    }
}

