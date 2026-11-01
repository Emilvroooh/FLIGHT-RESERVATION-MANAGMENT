package com.flightbooking.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

final class CreateAdminDialog extends JDialog {
    CreateAdminDialog(AppFrame frame){
        super(frame,"Set up administrator",true);setSize(450,440);setLocationRelativeTo(frame);
        JPanel p=new JPanel();p.setBackground(Color.WHITE);p.setBorder(new EmptyBorder(25,30,25,30));p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));
        p.add(Ui.title("First administrator"));p.add(Box.createVerticalStrut(9));p.add(Ui.muted("This setup is available only while no admin exists."));p.add(Box.createVerticalStrut(19));
        JTextField user=Ui.field(22);JPasswordField password=Ui.password(),passkey=Ui.password();
        user.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));password.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));passkey.setMaximumSize(new Dimension(Integer.MAX_VALUE,38));
        p.add(Ui.formField("Username",user));p.add(Box.createVerticalStrut(10));p.add(Ui.formField("Password (8+ characters)",password));p.add(Box.createVerticalStrut(10));p.add(Ui.formField("Admin Passkey",passkey));p.add(Box.createVerticalStrut(18));
        JButton create=Ui.button("Create administrator",true);p.add(create);
        create.addActionListener(e->{char[] secret=password.getPassword(),key=passkey.getPassword();try{frame.authService().createAdmin(user.getText(),secret,key);Ui.info(this,"Administrator created. Sign in with this username.");dispose();}catch(Exception ex){Ui.error(this,ex.getMessage());}finally{Arrays.fill(secret,'\0');Arrays.fill(key,'\0');password.setText("");passkey.setText("");}});
        setContentPane(p);
    }
}
