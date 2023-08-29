package com.suslov.cft.chat.client.views;

import javax.swing.*;
import java.awt.*;

public class NameWindow extends JDialog {

    private NameListener nameListener;
    private String enteredName = "";

    public NameWindow(JFrame frame) {
        super(frame, "Welcome to the chat", true);

        JTextField nameField = new JTextField();

        GridLayout layout = new GridLayout(3, 1);
        Container contentPane = getContentPane();
        contentPane.setLayout(layout);

        contentPane.add(new JLabel("Enter your name:"));
        contentPane.add(nameField);
        contentPane.add(createOkButton(nameField));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(210, 120));
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(false);
    }

    public void setNameListener(NameListener nameListener) {
        this.nameListener = nameListener;
    }

    private JButton createOkButton(JTextField nameField) {
        JButton button = new JButton("OK");
        button.addActionListener(e -> {
            enteredName = nameField.getText();
            if (nameListener != null && !enteredName.isBlank()) {
                dispose();
                nameListener.onNameEntered(enteredName);
            }
        });
        return button;
    }

    @Override
    public void dispose() {
        if (!enteredName.isBlank()) {
            super.dispose();
        }
    }
}