package com.suslov.cft.chat.client.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.List;

public class MainWindow extends JFrame {

    private JTextArea incoming;
    private JTextArea users;
    private JTextField outgoing;
    private JMenuItem newChatMenu;
    private JMenuItem settingsMenu;
    private JMenuItem exitMenu;
    private ButtonListener buttonListener;
    private ExitListener exitListener;

    public MainWindow(String title) throws HeadlessException {
        super(title);
        initializeWindow();
    }

    private void initializeWindow() {
        incoming = new JTextArea(25, 50);
        incoming.setLineWrap(true);
        incoming.setWrapStyleWord(true);
        incoming.setEditable(false);

        JScrollPane incomingScroller = new JScrollPane(incoming);
        incomingScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        incomingScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        users = new JTextArea(25, 20);
        users.setLineWrap(true);
        users.setWrapStyleWord(true);
        users.setEditable(false);

        JScrollPane userScroller = new JScrollPane(users);
        userScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        userScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        outgoing = new JTextField(64);
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(e -> {
            buttonListener.onButtonEntered(outgoing.getText());
            outgoing.setText("");
            outgoing.requestFocusInWindow();
        });

        JPanel upPanel = new JPanel();
        upPanel.add(incomingScroller);
        upPanel.add(userScroller);

        JPanel mainPanel = new JPanel();
        mainPanel.add(outgoing);
        mainPanel.add(sendButton);

        createMenu();

        getContentPane().add(BorderLayout.NORTH, upPanel);
        getContentPane().add(BorderLayout.CENTER, mainPanel);

        setSize(850, 550);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Menu");

        gameMenu.add(newChatMenu = new JMenuItem("Enter to chat"));
        gameMenu.add(settingsMenu = new JMenuItem("Connection settings"));
        gameMenu.addSeparator();
        gameMenu.add(exitMenu = new JMenuItem("Exit"));

        setExitMenuAction(exit-> dispose());

        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }

    public void setNewChatMenuAction(ActionListener listener) {
        newChatMenu.addActionListener(listener);
    }

    public void setSettingsMenuAction(ActionListener listener) {
        settingsMenu.addActionListener(listener);
    }

    private void setExitMenuAction(ActionListener listener) {
        exitMenu.addActionListener(listener);
    }

    public void setSendButtonAction(ButtonListener listener) {
        this.buttonListener = listener;
    }

    public void setExitListener(ExitListener listener) {
        exitListener = listener;
    }

    public void appendUsers(List<String> users) {
        this.users.setText("");
        for (String username : users) {
            this.users.append(username + "\n");
        }
    }

    public void appendUserMessage(String userName, String formattedDate, String text) {
        incoming.append(userName + "\n");
        incoming.append("(" + formattedDate + ") " + text + "\n");
    }

    public void appendServiceMessage(String text) {
        incoming.append(text + "\n");
    }

    @Override
    public void dispose() {
        if (exitListener != null) {
            exitListener.onDisposeWindow();
            super.dispose();
        }
    }
}