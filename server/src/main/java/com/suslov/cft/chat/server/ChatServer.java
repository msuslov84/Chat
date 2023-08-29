package com.suslov.cft.chat.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.suslov.cft.chat.common.Message;
import com.suslov.cft.chat.common.exceptions.ConnectException;
import com.suslov.cft.chat.common.service.Connection;
import com.suslov.cft.chat.common.service.PropertyUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.suslov.cft.chat.common.Message.Type.*;

@Slf4j
public class ChatServer {

    private final Map<ClientConnection, String> clients;
    private final List<String> usersInOrder;
    private final ObjectMapper mapper;

    public ChatServer() {
        this.clients = new ConcurrentHashMap<>();
        this.usersInOrder = Collections.synchronizedList(new ArrayList<>());
        this.mapper = new ObjectMapper();
    }

    public void launch() {
        try {
            start();
        } catch (ConnectException exp) {
            log.error(exp.getMessage() + " : " + exp.getCause().getMessage());
        }
    }

    private void start() {
        Connection connection = PropertyUtil.getConnection();
        try (ServerSocket serverSocket = new ServerSocket(connection.port())) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                try {
                    clients.put(new ClientConnection(clientSocket), "");
                    log.info("[SERVER] Client connection from host '" + clientSocket.getInetAddress().getHostName()
                            + "' and port '" + clientSocket.getPort() + "' has added");
                } catch (IOException e) {
                    log.warn("[SERVER] Client connection error: " + e.getMessage());
                    clientSocket.close();
                }
            }
        } catch (IOException e) {
            throw new ConnectException("[SERVER] Server startup error", e);
        }
    }

    class ClientConnection extends Thread {
        private final Socket clientSocket;
        private final BufferedReader in;
        private final BufferedWriter out;

        public ClientConnection(Socket clientSocket) throws IOException {
            this.clientSocket = clientSocket;
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            start();
        }

        @Override
        public void run() {
            try {
                String inputMessage;
                while ((inputMessage = in.readLine()) != null) {
                    Message message = mapper.readValue(inputMessage, Message.class);
                    processReceivedMessage(message);
                }
                closeConnection();
            } catch (IOException e) {
                log.warn("[SERVER] Error reading incoming message from client by host '" + clientSocket.getInetAddress().getHostName()
                        + "' and port '" + clientSocket.getPort() + "': " + e.getMessage());
            }
        }

        private void processReceivedMessage(Message message) {
            switch (message.getType()) {
                case USER_NAME -> acceptNewUserName(message.getUserName());
                case USER_TEXT -> acceptNewMessage(message);
            }
        }

        private void acceptNewUserName(String userName) {
            if (usersInOrder.contains(userName)) {
                this.sendMessage(new Message(ERROR_NAME, userName, "User with this name is " +
                        "already registered in the chat"));
            } else {
                addNewUserToList(userName);
            }
        }

        private void addNewUserToList(String userName) {
            clients.put(this, userName);
            usersInOrder.add(userName);
            log.info("[SERVER] Register new user: '" + userName + "'");
            for (ClientConnection client : clients.keySet()) {
                client.sendMessage(new Message(WELCOME_USER, userName, createServiceMessage("Welcome: '" +
                        userName + "' has joined the chat!")));
                client.sendUsers();
            }
        }

        private void acceptNewMessage(Message message) {
            log.info("[SERVER] Send message from '" + message.getUserName() + "': '" + message.getText() + "'");
            for (ClientConnection client : clients.keySet()) {
                client.sendMessage(message);
            }
        }

        private void sendUsers() {
            try {
                String names = String.join(";", usersInOrder);
                out.write(mapper.writeValueAsString(new Message(USER_NAME, names)) + "\n");
                out.flush();
            } catch (IOException e) {
                log.warn("[SERVER] Error sending user list to client by host '" + clientSocket.getInetAddress().getHostName()
                        + "' and port '" + clientSocket.getPort() + "': " + e.getMessage());
            }
        }

        private void sendMessage(Message message) {
            try {
                out.write(mapper.writeValueAsString(message) + "\n");
                out.flush();
            } catch (IOException e) {
                log.warn("[SERVER] Error sending message to client by host '" + clientSocket.getInetAddress().getHostName()
                        + "' and port '" + clientSocket.getPort() + "': " + e.getMessage());
            }
        }

        private void closeConnection() {
            partingToUser();
            close();
        }

        private void partingToUser() {
            String userName = clients.remove(this);
            usersInOrder.remove(userName);
            log.info("[SERVER] Delete user: '" + userName + "'");
            for (ClientConnection client : clients.keySet()) {
                client.sendMessage(new Message(PARTING_USER, userName, createServiceMessage("Goodbye: '" +
                        userName + "' has parted from the chat!")));
                client.sendUsers();
            }
        }

        private String createServiceMessage(String text) {
            return "-".repeat(50) + "\n" + text + "\n" + "-".repeat(50) + "\n";
        }

        private void close() {
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
                log.info("[SERVER] Client connection by host '" + clientSocket.getInetAddress().getHostName()
                        + "' and port '" + clientSocket.getPort() + "' has successfully closed");
            } catch (IOException e) {
                log.error("[SERVER] Error closing client connection by host '" + clientSocket.getInetAddress().getHostName()
                        + "' and port '" + clientSocket.getPort() + "': " + e.getMessage());
            }
        }
    }
}