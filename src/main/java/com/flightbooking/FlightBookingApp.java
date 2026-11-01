package com.flightbooking;

import com.flightbooking.ui.AppFrame;
import javax.swing.*;
import java.awt.*;

public final class FlightBookingApp {
    private FlightBookingApp() {}
    public static void main(String[] args){
        try{for(UIManager.LookAndFeelInfo info:UIManager.getInstalledLookAndFeels())if("Nimbus".equals(info.getName())){UIManager.setLookAndFeel(info.getClassName());break;}}
        catch(Exception ignored){}
        UIManager.put("defaultFont",new Font("Segoe UI",Font.PLAIN,14));
        SwingUtilities.invokeLater(()->new AppFrame().setVisible(true));
    }
}
