package com.suslov.cft.chat.client.views;

import javax.swing.*;
import java.awt.*;

public class SettingsWindow extends JFrame {

    private final JTextField host;
    private final JTextField port;
    private ButtonListener buttonListener;

    public SettingsWindow() {
        super("Connection settings");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel();
        setContentPane(panel);

        BoxLayout layout = new BoxLayout(panel, BoxLayout.Y_AXIS);
        getContentPane().setLayout(layout);

        JPanel hostPanel = new JPanel();
        JLabel lHost = new JLabel("Host: ");
        hostPanel.add(lHost);
        host = new JTextField("", 30);
        lHost.setLabelFor(host);
        hostPanel.add(host);

        JPanel portPanel = new JPanel();
        JLabel lPort = new JLabel("Port: ");
        portPanel.add(lPort);
        port = new JTextField("", 5);
        lPort.setLabelFor(port);
        portPanel.add(port);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createOkButton());
        buttonPanel.add(createCloseButton());

        panel.add(hostPanel);
        panel.add(portPanel);
        panel.add(buttonPanel);

        setSize(400, 130);
        pack();
        setVisible(false);
    }

    public void setHost(String host) {
        this.host.setText(host);
    }

    public void setPort(String port) {
        this.port.setText(port);
    }

    public void setOkButtonAction(ButtonListener listener) {
        this.buttonListener = listener;
    }

    private JButton createOkButton() {
        JButton okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(80, 25));
        okButton.addActionListener(e -> {
            dispose();
            if (buttonListener != null) {
                buttonListener.onButtonEntered(host.getText() + ";" + port.getText());
            }
        });
        return okButton;
    }

    private JButton createCloseButton() {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setPreferredSize(new Dimension(80, 25));
        cancelButton.addActionListener(e -> dispose());
        return cancelButton;
    }
}
