package ru.shift;


import java.io.IOException;

public class ServerApplication {
    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer(PropertiesReader.readProperties());
        server.start();
    }
}
