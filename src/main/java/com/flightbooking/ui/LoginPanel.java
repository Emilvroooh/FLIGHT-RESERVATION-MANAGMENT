package com.flightbooking.ui;

import com.flightbooking.model.Account;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

final class LoginPanel extends JPanel {
    private static final Insets ZERO = new Insets(0, 0, 0, 0);

    LoginPanel(AppFrame frame) {
        super(new GridBagLayout());
        setBackground(Ui.CANVAS);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Ui.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Ui.INPUT_BORDER), new EmptyBorder(0, 0, 0, 0)));
        card.setPreferredSize(new Dimension(900, 560));
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0; c.fill = GridBagConstraints.BOTH; c.weighty = 1;
        c.gridx = 0; c.weightx = .43; card.add(brandPanel(), c);
        c.gridx = 1; c.weightx = .57; card.add(formPanel(frame), c);
        GridBagConstraints outer = new GridBagConstraints();
        outer.gridx = 0; outer.gridy = 0; outer.weightx = 1; outer.weighty = 1;
        outer.fill = GridBagConstraints.BOTH; outer.insets = new Insets(28, 36, 28, 36);
        add(card, outer);
    }

    private JPanel brandPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Ui.DARK);
        panel.setBorder(new EmptyBorder(42, 38, 38, 38));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        JLabel logo = new JLabel("✈  SKYWARD");
        logo.setForeground(Ui.WHITE); logo.setFont(new Font("Segoe UI", Font.BOLD, 19));
        c.gridy = 0; c.weighty = 0; panel.add(logo, c);
        c.gridy = 1; c.weighty = 1; panel.add(Box.createVerticalGlue(), c);
        JLabel headline = new JLabel("<html>Find your<br>next horizon.</html>");
        headline.setForeground(Ui.WHITE); headline.setFont(new Font("Segoe UI", Font.BOLD, 35));
        c.gridy = 2; c.weighty = 0; panel.add(headline, c);
        JLabel note = new JLabel("<html>Search flights, book seats, and keep<br>your trip details close.</html>");
        note.setForeground(new Color(209, 213, 219)); note.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.gridy = 3; c.insets = new Insets(15, 0, 0, 0); panel.add(note, c);
        c.gridy = 4; c.weighty = 1; c.insets = ZERO; panel.add(Box.createVerticalGlue(), c);
        JLabel footer = new JLabel("YOUR JOURNEY STARTS HERE");
        footer.setForeground(new Color(156, 163, 175)); footer.setFont(new Font("Segoe UI", Font.BOLD, 10));
        c.gridy = 5; c.weighty = 0; panel.add(footer, c);
        return panel;
    }

    private JPanel formPanel(AppFrame frame) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Ui.WHITE);
        panel.setBorder(new EmptyBorder(38, 48, 30, 48));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST; c.insets = ZERO;

        JLabel eyebrow = new JLabel("WELCOME ABOARD");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11)); eyebrow.setForeground(Ui.ACCENT);
        c.gridy = 0; panel.add(eyebrow, c);
        JLabel heading = Ui.title("Sign in");
        c.gridy = 1; c.insets = new Insets(7, 0, 2, 0); panel.add(heading, c);
        JLabel subtitle = Ui.muted("Choose how you want to access Skyward.");
        c.gridy = 2; c.insets = new Insets(0, 0, 20, 0); panel.add(subtitle, c);

        JRadioButton passenger = new JRadioButton("Passenger", true);
        JRadioButton admin = new JRadioButton("Administrator");
        ButtonGroup roles = new ButtonGroup(); roles.add(passenger); roles.add(admin);
        styleRole(passenger, true); styleRole(admin, false);
        JPanel roleBar = new JPanel(new GridLayout(1, 2, 8, 0));
        roleBar.setOpaque(false); roleBar.add(passenger); roleBar.add(admin);
        c.gridy = 3; c.insets = new Insets(0, 0, 19, 0); panel.add(roleBar, c);

        JTextField login = Ui.field(22);
        JPasswordField password = Ui.password();
        c.gridy = 4; c.insets = ZERO; panel.add(Ui.formField("Email address", login), c);
        c.gridy = 5; c.insets = new Insets(15, 0, 0, 0); panel.add(Ui.formField("Password", password), c);

        JButton signIn = Ui.button("Sign in", true);
        c.gridy = 6; c.insets = new Insets(22, 0, 0, 0); panel.add(signIn, c);
        JButton forgot = linkButton("Forgot password?");
        c.gridy = 7; c.insets = new Insets(7, 0, 0, 0); panel.add(forgot, c);
        JButton create = Ui.button("Create user account", false);
        c.gridy = 8; c.insets = new Insets(5, 0, 0, 0); panel.add(create, c);
        JButton firstAdmin = linkButton("Set up first administrator");
        try { firstAdmin.setVisible(frame.accountDao().adminCount() == 0); }
        catch (Exception ex) { firstAdmin.setVisible(false); }
        c.gridy = 9; c.insets = new Insets(4, 0, 0, 0); panel.add(firstAdmin, c);
        JLabel note = Ui.muted("Payments are simulated for this demo.");
        c.gridy = 10; c.insets = new Insets(13, 0, 0, 0); panel.add(note, c);

        Runnable updateRole = () -> {
            boolean isAdmin = admin.isSelected();
            styleRole(passenger, !isAdmin); styleRole(admin, isAdmin);
        };
        passenger.addActionListener(e -> updateRole.run());
        admin.addActionListener(e -> updateRole.run());
        // Ui.formField owns its label, so swap the field wrapper when the selected role changes.
        Runnable replaceLoginField = () -> {
            Component old = panel.getComponent(4);
            panel.remove(old);
            GridBagConstraints fieldConstraints = (GridBagConstraints)c.clone();
            fieldConstraints.gridy = 4; fieldConstraints.insets = ZERO;
            panel.add(Ui.formField(admin.isSelected() ? "Administrator username" : "Email address", login), fieldConstraints, 4);
            panel.revalidate(); panel.repaint();
        };
        passenger.addActionListener(e -> replaceLoginField.run());
        admin.addActionListener(e -> replaceLoginField.run());

        signIn.addActionListener(e -> {
            char[] secret = password.getPassword();
            char[] adminPasskey = null;
            try {
                if(admin.isSelected()){
                    adminPasskey=promptAdminPasskey();
                    if(adminPasskey==null){Ui.error(this,"Invalid Admin Passkey");return;}
                    if(adminPasskey.length==0){Ui.error(this,"Invalid Admin Passkey");return;}
                }
                Account account = frame.authService().login(login.getText().trim(), secret, adminPasskey);
                if (account == null) {
                    Ui.error(this, "Login was not recognized. Check your username and password.");
                } else if (account.isAdmin() != admin.isSelected()) {
                    Ui.error(this, account.isAdmin()
                            ? "This is an administrator account. Select Administrator to sign in."
                            : "This is a passenger account. Select Passenger to sign in.");
                } else {
                    frame.openAccount(account);
                }
            } catch (SecurityException ex) { Ui.error(this,"Invalid Admin Passkey"); }
            catch (Exception ex) { Ui.error(this, ex.getMessage()); }
            finally { Arrays.fill(secret, '\0');if(adminPasskey!=null)Arrays.fill(adminPasskey,'\0');password.setText(""); }
        });
        password.addActionListener(e -> signIn.doClick());
        create.addActionListener(e -> frame.registerDialog());
        forgot.addActionListener(e -> frame.forgotPasswordDialog(login.getText().trim()));
        firstAdmin.addActionListener(e -> frame.adminSetupDialog());
        return panel;
    }

    private char[] promptAdminPasskey(){
        JPasswordField passkey=Ui.password();
        JPanel form=new JPanel(new BorderLayout(0,8));form.setBorder(new EmptyBorder(8,8,8,8));
        form.add(Ui.formField("Admin Passkey",passkey),BorderLayout.CENTER);
        int result=JOptionPane.showConfirmDialog(this,form,"Administrator verification",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE);
        if(result!=JOptionPane.OK_OPTION){char[] discarded=passkey.getPassword();Arrays.fill(discarded,'\0');return null;}
        return passkey.getPassword();
    }

    private static JButton linkButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12)); button.setForeground(Ui.ACCENT);
        button.setBorderPainted(false); button.setContentAreaFilled(false); button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); button.setHorizontalAlignment(SwingConstants.LEFT);
        return button;
    }

    private static void styleRole(JRadioButton button, boolean selected) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 12)); button.setForeground(selected ? Ui.ACCENT : Ui.MUTED);
        button.setBackground(selected ? new Color(230, 244, 241) : Ui.WHITE);
        button.setOpaque(true); button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(selected ? Ui.ACCENT : Ui.INPUT_BORDER), new EmptyBorder(10, 12, 10, 12)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
