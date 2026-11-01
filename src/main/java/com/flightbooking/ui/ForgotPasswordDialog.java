package com.flightbooking.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

final class ForgotPasswordDialog extends JDialog {
    ForgotPasswordDialog(AppFrame frame, String email) {
        super(frame, "Reset passenger password", true);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Ui.WHITE);
        content.setBorder(new EmptyBorder(24, 28, 22, 28));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST;
        JLabel heading = Ui.title("Reset passenger password");
        c.gridy = 0; c.insets = new Insets(0, 0, 8, 0); content.add(heading, c);
        JLabel message = new JLabel("<html>User, you can now change your passenger password by entering a new one below.</html>");
        message.setFont(Ui.BODY); message.setForeground(Ui.MUTED);
        c.gridy = 1; c.insets = new Insets(0, 0, 18, 0); content.add(message, c);
        JTextField emailField = Ui.field(24);
        emailField.setText(email == null ? "" : email);
        JPasswordField newPassword = Ui.password(), confirmPassword = Ui.password();
        c.gridy = 2; c.insets = new Insets(0, 0, 12, 0); content.add(Ui.formField("Passenger email", emailField), c);
        c.gridy = 3; content.add(Ui.formField("New Password", newPassword), c);
        c.gridy = 4; c.insets = new Insets(0, 0, 0, 0); content.add(Ui.formField("Confirm New Password", confirmPassword), c);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actions.setOpaque(false);
        JButton cancel = Ui.button("Cancel", false), reset = Ui.button("Reset Password", true);
        actions.add(cancel); actions.add(reset);
        JPanel root = new JPanel(new BorderLayout(0, 8)); root.setBackground(Ui.WHITE);
        JScrollPane scroll = new JScrollPane(content, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getViewport().setBackground(Ui.WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);
        c.gridy = 5; c.insets = new Insets(12, 28, 16, 28); actions.setBorder(new EmptyBorder(0, 0, 0, 0));
        root.add(actions, BorderLayout.SOUTH);
        cancel.addActionListener(e -> dispose());
        reset.addActionListener(e -> {
            char[] password = newPassword.getPassword(), confirmation = confirmPassword.getPassword();
            try {
                frame.authService().resetPassengerPassword(emailField.getText().trim(), password, confirmation);
                Ui.info(this, "Passenger password updated. You can now sign in with the new password.");
                dispose();
            } catch (Exception ex) { Ui.error(this, ex.getMessage()); }
            finally { Arrays.fill(password, '\0'); Arrays.fill(confirmation, '\0'); newPassword.setText(""); confirmPassword.setText(""); }
        });
        getRootPane().setDefaultButton(reset);
        setContentPane(root); setSize(520, 350); setMinimumSize(new Dimension(430, 260)); setLocationRelativeTo(frame);
    }
}
