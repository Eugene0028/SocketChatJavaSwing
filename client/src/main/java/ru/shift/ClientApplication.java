package ru.shift;


import ru.shift.controller.ViewController;
import ru.shift.model.ClientModel;
import ru.shift.view.ChatClientGUI;

public class ClientApplication {
    public static void main(String[] args) {
        ClientModel model = new ClientModel();
        ChatClientGUI view = new ChatClientGUI();
        ViewController controller = new ViewController(model, view);
    }
}

