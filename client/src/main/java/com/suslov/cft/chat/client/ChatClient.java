package com.suslov.cft.chat.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suslov.cft.chat.client.views.ErrorNameWindow;
import com.suslov.cft.chat.client.views.MainWindow;
import com.suslov.cft.chat.client.views.NameWindow;
import com.suslov.cft.chat.client.views.SettingsWindow;
import com.suslov.cft.chat.common.Message;
import com.suslov.cft.chat.common.service.Connection;
import com.suslov.cft.chat.common.service.PropertyUtil;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.suslov.cft.chat.common.Message.Type.USER_NAME;
import static com.suslov.cft.chat.common.Message.Type.USER_TEXT;

@Slf4j
public class ChatClient {
    private MainWindow mainWindow;
    private ErrorNameWindow errorNameWindow;
    private SettingsWindow settingsWindow;
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private String userName;
    private List<String> users;
    private final ObjectMapper mapper = new ObjectMapper();
    private Connection connection;

    public ChatClient() {
        initializeContent();
    }

    public void launch() {
        initializeWindowActions();
    }

    private void initializeContent() {
        users = new ArrayList<>();
        mainWindow = new MainWindow("Chat");
        errorNameWindow = new ErrorNameWindow(mainWindow, "User with this name is already " +
                "registered in the chat");
        settingsWindow = new SettingsWindow();
    }

    private void initializeWindowActions() {
        mainWindow.setNewChatMenuAction(e -> launchConnectionToChat());
        mainWindow.setSettingsMenuAction(e -> {
            settingsWindow.setVisible(true);
            settingsWindow.setHost(connection == null ? "" : connection.host());
            settingsWindow.setPort(connection == null ? "" : String.valueOf(connection.port()));
        });
        mainWindow.setSendButtonAction(text -> {
            if (text.isBlank() || out == null) {
                return;
            }
            try {
                out.write(mapper.writeValueAsString(new Message(USER_TEXT, userName, text)) + "\n");
                out.flush();
            } catch (IOException e) {
                log.warn("[CLIENT] Message sending error: " + e.getMessage());
            }
        });
        mainWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mainWindow.dispose();
            }
        });
        mainWindow.setExitListener(this::close);
        settingsWindow.setOkButtonAction(this::setConnectionSettings);
        errorNameWindow.setOkListener(e -> enterUserName());
    }

    private void launchConnectionToChat() {
        if (setUpServerConnection()) {
            startIncomingThread();
            enterUserName();
        }
    }

    private boolean setUpServerConnection() {
        connection = (connection == null ? PropertyUtil.getConnection() : connection);
        try {
            socket = new Socket(connection.host(), connection.port());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            log.info("[CLIENT] Connection with server by host '" + connection.host() + "' and port '" +
                    connection.port() + "' has established");
            return true;
        } catch (IOException e) {
            log.error("[CLIENT] Server connection error", e);
            return false;
        }
    }

    private void startIncomingThread() {
        new Thread(new IncomingReader()).start();
    }

    private void enterUserName() {
        presentNameWindow();
        if (userName != null) {
            sendUserNameToServer();
        }
    }

    private void presentNameWindow() {
        NameWindow nameWindow = new NameWindow(mainWindow);
        nameWindow.setNameListener(name -> userName = name);
        nameWindow.setVisible(true);
        nameWindow.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    }

    private void sendUserNameToServer() {
        try {
            out.write(mapper.writeValueAsString(new Message(USER_NAME, userName)) + "\n");
            out.flush();
        } catch (IOException e) {
            userName = null;
            log.warn("[CLIENT] Error sending the entered name: " + e.getMessage());
        }
    }

    private void setConnectionSettings(String userSettings) {
        String[] settings = userSettings.split(";");
        if (settings.length == 2) {
            connection = PropertyUtil.getConnectionFromString(settings[0], settings[1]);
            log.info("[CLIENT] Connection setting has changed on host '" + connection.host() + "' and port '" +
                    connection.port() + "'");
        } else {
            log.warn("[CLIENT] Error entering connection setting");
        }
    }

    private void close() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                log.info("[CLIENT] Socket connection has successfully closed");
            } catch (IOException e) {
                log.error("[CLIENT] Error closing socket connection: " + e.getMessage());
            }
        }
    }

    private class IncomingReader implements Runnable {
        @Override
        public void run() {
            try {
                String inputMessage;
                while ((inputMessage = in.readLine()) != null) {
                    Message message = mapper.readValue(inputMessage, Message.class);
                    processReceivedMessage(message);
                }
            } catch (IOException e) {
                log.warn("[CLIENT] Error reading incoming message: " + e.getMessage());
            }
        }

        private void processReceivedMessage(Message message) {
            switch (message.getType()) {
                case ERROR_NAME -> reportWrongNameEntered();
                case WELCOME_USER, PARTING_USER -> addServiceMessageToChat(message.getText());
                case USER_NAME -> addNewUserToList(message.getUserName());
                case USER_TEXT -> addNewMessageToChat(message);
            }
        }

        private void reportWrongNameEntered() {
            userName = null;
            errorNameWindow.setVisible(true);
        }

        private void addServiceMessageToChat(String text) {
            mainWindow.appendServiceMessage(text);
        }

        private void addNewUserToList(String userNames) {
            users = List.of(userNames.split(";"));
            mainWindow.appendUsers(users);
        }

        private void addNewMessageToChat(Message message) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String formattedDate = dateFormat.format(new Date());
            mainWindow.appendUserMessage(message.getUserName(), formattedDate, message.getText());
        }
    }
}