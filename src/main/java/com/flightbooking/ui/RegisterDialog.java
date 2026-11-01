package com.flightbooking.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

final class RegisterDialog extends JDialog {
    RegisterDialog(AppFrame frame){super(frame,"Create account",true);setDefaultCloseOperation(DISPOSE_ON_CLOSE);setResizable(false);JPanel panel=new JPanel();panel.setBackground(Color.WHITE);panel.setBorder(new EmptyBorder(24,30,24,30));panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));panel.add(Ui.title("Create your account"));panel.add(Box.createVerticalStrut(18));
        JTextField name=Ui.field(25),email=Ui.field(25),phone=Ui.field(25);JPasswordField password=Ui.password();for(JComponent field:new JComponent[]{name,email,phone,password})field.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));
        panel.add(Ui.formField("Full name",name));panel.add(Box.createVerticalStrut(10));panel.add(Ui.formField("Email",email));panel.add(Box.createVerticalStrut(10));panel.add(Ui.formField("Phone (optional)",phone));panel.add(Box.createVerticalStrut(10));panel.add(Ui.formField("Password (8+ characters)",password));panel.add(Box.createVerticalStrut(18));JButton create=Ui.button("Create account",true);create.setAlignmentX(Component.LEFT_ALIGNMENT);create.setMaximumSize(new Dimension(Integer.MAX_VALUE,42));panel.add(create);create.addActionListener(e->{char[] p=password.getPassword();try{frame.authService().register(name.getText(),email.getText(),phone.getText(),p);Ui.info(this,"Account created. Sign in using this email address.");dispose();}catch(Exception ex){Ui.error(this,ex.getMessage()==null?"Account could not be created. Check the database connection and entered values.":ex.getMessage());}finally{Arrays.fill(p,'\0');}});setContentPane(panel);
    }
}
