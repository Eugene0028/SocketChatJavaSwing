package ru.shift.view;

import lombok.Setter;
import ru.shift.Message;
import ru.shift.protocol.ResponseStatus;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class ChatClientGUI {
    private JFrame connectFrame;
    private JFrame chatFrame;
    private JTextField serverIPField;
    private JTextField serverPortField;
    private JTextField usernameField;
    private JTextArea messageArea;
    private JTextField inputField;
    private JList<String> userList;
    private JButton sendButton;
    private JButton connectButton;
    private JLabel statusLabel;

    @Setter
    private ConnectButtonEventListener buttonListener;
    @Setter
    private SendMessageEventListener messageEventListener;

    public ChatClientGUI() {
        initConnectWindow();
    }

    public void initConnectWindow() {
        if (chatFrame != null && chatFrame.isVisible()) {
            chatFrame.dispose();
        }
        if (connectFrame != null) {
            connectFrame.setVisible(true);
        } else {
            showServerConnectionWindow(); //типа первое включение
        }
    }

    public void visualiseConnectStatus(ResponseStatus status) {
        switch (status) {
            case OK -> {
                System.out.println(usernameField.getText());
                connectFrame.setVisible(false);
                showChatWindow();
            }
            case USERNAME_ERROR -> showError("CONNECT: NO. THIS USERNAME ALREADY USED");
            case FAILED -> showError("CONNECT: NO. WRONG IP or PORT");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(connectFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showServerConnectionWindow() {
        connectFrame = new JFrame("Connect to Server");
        connectFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        connectFrame.setSize(400, 250);
        connectFrame.setLayout(new GridLayout(4, 2, 5, 5));
        connectFrame.setLocationRelativeTo(null);

        JLabel ipLabel = new JLabel("Server IP:");
        serverIPField = new JTextField("0.0.0.0");

        JLabel portLabel = new JLabel("Port:");
        serverPortField = new JTextField("8080");

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();

        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> {
            String serverIP = serverIPField.getText();
            String serverPort = serverPortField.getText();
            String username = usernameField.getText();

            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(connectFrame, "Please enter a username!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                buttonListener.connectEvent(serverIP, serverPort, username);
            }
        });

        statusLabel = new JLabel("CONNECT: WAITING...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);

        connectFrame.add(ipLabel);
        connectFrame.add(serverIPField);
        connectFrame.add(portLabel);
        connectFrame.add(serverPortField);
        connectFrame.add(usernameLabel);
        connectFrame.add(usernameField);
        connectFrame.add(statusLabel);
        connectFrame.add(connectButton);

        connectFrame.setVisible(true);
    }

    private void showChatWindow() {
        if (chatFrame == null) {
            chatFrame = new JFrame("Chat");
            chatFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            chatFrame.setSize(900, 400);
            chatFrame.setLocationRelativeTo(null);
            chatFrame.setLayout(new BorderLayout(5, 5));

            messageArea = new JTextArea();
            messageArea.setEditable(false);
            JScrollPane messageScroll = new JScrollPane(messageArea);

            JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
            inputField = new JTextField();
            sendButton = new JButton("Send");
            sendButton.addActionListener(e -> sendMessage());
            inputPanel.add(inputField, BorderLayout.CENTER);
            inputPanel.add(sendButton, BorderLayout.EAST);

            userList = new JList<>(new DefaultListModel<>());
            JScrollPane userScroll = new JScrollPane(userList);
            userScroll.setPreferredSize(new Dimension(150, 0));

            chatFrame.add(messageScroll, BorderLayout.CENTER);
            chatFrame.add(inputPanel, BorderLayout.SOUTH);
            chatFrame.add(userScroll, BorderLayout.EAST);

            inputField.addKeyListener(new KeyAdapter() {
                public void keyPressed(KeyEvent evt) {
                    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                        sendMessage();
                    }
                }
            });


            chatFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowActivated(java.awt.event.WindowEvent e) {
                    inputField.requestFocusInWindow();
                }
            });
        }
        chatFrame.setVisible(true);


        inputField.requestFocusInWindow();
    }


    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    public void visualiseNewMessage(Message message) {
        String formattedDate = formatter.format(Instant.ofEpochMilli(message.getTimestamp().getTime()));
        String form = String.format("time: [%s] | username: [%s] | content: [%s]\n", formattedDate, message.getSender(), message.getMessage());
        messageArea.append(form);
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            messageEventListener.sendMessageEvent(message);
            inputField.setText("");
        }
    }

    public void updateUserNamesWindow(Set<String> usernames) {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        usernames.forEach(x -> {
            if (usernameField.getText().equals(x)) {
                listModel.addElement("You are: " + x);
            } else {
                listModel.addElement(x);
            }
        });
        userList.setModel(listModel);
    }

}