package com.suslov.cft.chat.client.views;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ErrorNameWindow extends JDialog {
    private ActionListener okListener;

    public ErrorNameWindow(JFrame owner, String errorText) {
        super(owner, "Authentication error", true);

        GridBagLayout layout = new GridBagLayout();
        Container contentPane = getContentPane();
        contentPane.setLayout(layout);

        contentPane.add(createTextLabel(layout, errorText));
        contentPane.add(createOkButton(layout));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(350, 130));
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(false);
    }

    public void setOkListener(ActionListener listener) {
        this.okListener = listener;
    }

    private JLabel createTextLabel(GridBagLayout layout, String text) {
        JLabel label = new JLabel(text);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        layout.setConstraints(label, gbc);
        return label;
    }

    private JButton createOkButton(GridBagLayout layout) {
        JButton okButton = new JButton("OK");
        okButton.setPreferredSize(new Dimension(100, 25));

        okButton.addActionListener(e -> {
            dispose();

            if (okListener != null) {
                okListener.actionPerformed(e);
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(15, 5, 0, 0);
        layout.setConstraints(okButton, gbc);

        return okButton;
    }
}