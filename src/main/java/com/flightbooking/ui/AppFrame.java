package com.flightbooking.ui;

import com.flightbooking.dao.*;
import com.flightbooking.model.Account;
import com.flightbooking.service.*;
import javax.swing.*;
import java.awt.*;

public class AppFrame extends JFrame {
    private final AccountDao accountDao=new AccountDao(); private final FlightDao flightDao=new FlightDao(); private final BookingDao bookingDao=new BookingDao();
    private final AuthService authService=new AuthService(accountDao); private final BookingService bookingService=new BookingService(bookingDao);
    public AppFrame(){super("Skyward | Flight Booking");setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);setMinimumSize(new Dimension(950,650));setSize(1120,760);getContentPane().setBackground(Ui.CANVAS);setLocationRelativeTo(null);showLogin();}
    public AccountDao accountDao(){return accountDao;} public FlightDao flightDao(){return flightDao;} public BookingDao bookingDao(){return bookingDao;} public AuthService authService(){return authService;} public BookingService bookingService(){return bookingService;}
    void showLogin(){setTitle("Skyward | Flight Booking");setContentPane(new LoginPanel(this));revalidate();repaint();}
    public void openAccount(Account account){
        if(account.isAdmin()){
            setTitle("Skyward | Admin Console");
            setSize(Math.max(getWidth(),1120),Math.max(getHeight(),760));
            setContentPane(new AdminPanel(this,account));
        }else{
            setTitle("Skyward | Flight Booking");
            setContentPane(new FlightSearchPanel(this,account));
        }
        revalidate();repaint();
    }
    public void signOut(){showLogin();}
    void registerDialog(){
        try{
            RegisterDialog dialog=new RegisterDialog(this);
            dialog.pack();
            dialog.setMinimumSize(new Dimension(460,520));
            dialog.setSize(460,520);
            dialog.setLocationRelativeTo(this);
            dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            dialog.toFront();
            dialog.setVisible(true);
        }catch(RuntimeException ex){Ui.error(this,"Could not open the registration form.\n"+ex.getMessage());}
    }
    void forgotPasswordDialog(String email){new ForgotPasswordDialog(this,email).setVisible(true);}
    void adminSetupDialog(){new CreateAdminDialog(this).setVisible(true);}
}
