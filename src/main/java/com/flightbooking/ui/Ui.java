package com.flightbooking.ui;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

final class Ui {
    static final Color CANVAS=Color.decode("#F8F9FA"), DARK=Color.decode("#1E2229"), ACCENT=Color.decode("#0F766E"), INK=Color.decode("#111827"), MUTED=Color.decode("#6B7280"), INPUT_BORDER=Color.decode("#E5E7EB"), WHITE=Color.decode("#FFFFFF");
    static final Color TEAL=ACCENT,TEAL_DARK=new Color(13,94,88),MINT=new Color(230,244,241),PAPER=CANVAS,LINE=INPUT_BORDER;
    static final Font BODY=new Font("Segoe UI",Font.PLAIN,14), HEADING=new Font("Segoe UI",Font.BOLD,24);
    private Ui(){}
    static JPanel panel(LayoutManager layout){JPanel p=new JPanel(layout);p.setBackground(CANVAS);return p;}
    static JLabel title(String text){JLabel l=new JLabel(text);l.setFont(HEADING);l.setForeground(INK);return l;}
    static JLabel muted(String text){JLabel l=new JLabel(text);l.setFont(new Font("Segoe UI",Font.PLAIN,13));l.setForeground(MUTED);return l;}
    static JButton button(String text,boolean primary){
        JButton b=new JButton(text);b.setFont(new Font("Segoe UI",Font.BOLD,13));b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(primary?ACCENT:INPUT_BORDER),new EmptyBorder(9,16,9,16)));
        Color base=primary?ACCENT:WHITE,hover=primary?new Color(12,92,86):new Color(243,245,247);b.setBackground(base);b.setForeground(primary?WHITE:INK);b.setOpaque(true);
        b.addMouseListener(new MouseAdapter(){@Override public void mouseEntered(MouseEvent e){if(b.isEnabled())b.setBackground(hover);}@Override public void mouseExited(MouseEvent e){b.setBackground(base);}});return b;
    }
    static JTextField field(int cols){JTextField f=new JTextField(cols);styleInput(f);return f;}
    static JPasswordField password(){JPasswordField f=new JPasswordField(22);styleInput(f);return f;}
    private static void styleInput(JTextField f){
        f.setFont(BODY);f.setForeground(INK);f.setBackground(WHITE);f.setCaretColor(ACCENT);
        Border normal=BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(INPUT_BORDER),new EmptyBorder(9,11,9,11));
        Border focus=BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(ACCENT,2),new EmptyBorder(8,10,8,10));
        f.setBorder(normal);f.addFocusListener(new FocusAdapter(){@Override public void focusGained(FocusEvent e){f.setBorder(focus);}@Override public void focusLost(FocusEvent e){f.setBorder(normal);}});
    }
    static JTable table(String... headers){DefaultTableModel model=new DefaultTableModel(headers,0){@Override public boolean isCellEditable(int row,int column){return false;}};JTable t=new JTable(model);t.setBackground(WHITE);t.setForeground(INK);t.setRowHeight(36);t.setFont(BODY);t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,12));t.getTableHeader().setBackground(MINT);t.getTableHeader().setForeground(INK);t.setSelectionBackground(new Color(204,235,229));t.setGridColor(LINE);t.setFillsViewportHeight(true);return t;}
    static DefaultTableModel model(JTable table){return (DefaultTableModel)table.getModel();}
    static void error(Component parent,String message){JOptionPane.showMessageDialog(parent,message,"Skyward",JOptionPane.ERROR_MESSAGE);}
    static void info(Component parent,String message){JOptionPane.showMessageDialog(parent,message,"Skyward",JOptionPane.INFORMATION_MESSAGE);}
    static JPanel formField(String label,JComponent field){return formField(label,field,INK);}
    static JPanel formFieldOnDark(String label,JComponent field){return formField(label,field,new Color(243,244,246));}
    private static JPanel formField(String label,JComponent field,Color labelColor){JPanel p=new JPanel(new BorderLayout(0,7));p.setOpaque(false);JLabel l=new JLabel(label);l.setFont(new Font("Segoe UI",Font.BOLD,12));l.setForeground(labelColor);p.add(l,BorderLayout.NORTH);p.add(field,BorderLayout.CENTER);return p;}
}
