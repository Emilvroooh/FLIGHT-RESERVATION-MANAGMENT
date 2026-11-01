package com.flightbooking.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

final class CalendarDialog extends JDialog {
    private final LocalDate today=LocalDate.now();
    private LocalDate selectedDate,shownMonth;
    private final JLabel monthLabel=new JLabel("",SwingConstants.CENTER);
    private final JPanel days=new JPanel(new GridLayout(0,7,4,4));
    private final DateTimeFormatter monthFormat=DateTimeFormatter.ofPattern("MMMM yyyy");
    CalendarDialog(Window owner,LocalDate current){
        super(owner,"Choose departure date",Dialog.ModalityType.APPLICATION_MODAL);
        selectedDate=current;shownMonth=(current==null?today:current).withDayOfMonth(1);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);setResizable(false);
        JPanel root=new JPanel(new BorderLayout(10,12));root.setBackground(Color.WHITE);root.setBorder(new EmptyBorder(16,16,14,16));
        JButton previous=navButton("‹"),next=navButton("›");monthLabel.setFont(new Font("Segoe UI",Font.BOLD,16));monthLabel.setForeground(Ui.INK);
        JPanel top=new JPanel(new BorderLayout());top.setOpaque(false);top.add(previous,BorderLayout.WEST);top.add(monthLabel,BorderLayout.CENTER);top.add(next,BorderLayout.EAST);
        days.setOpaque(false);
        JPanel bottom=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));bottom.setOpaque(false);JButton clear=Ui.button("Clear date",false),todayButton=Ui.button("Today",false);bottom.add(clear);bottom.add(todayButton);
        root.add(top,BorderLayout.NORTH);root.add(days,BorderLayout.CENTER);root.add(bottom,BorderLayout.SOUTH);setContentPane(root);
        previous.addActionListener(e->{shownMonth=shownMonth.minusMonths(1);renderDays();});next.addActionListener(e->{shownMonth=shownMonth.plusMonths(1);renderDays();});
        clear.addActionListener(e->{selectedDate=null;dispose();});todayButton.addActionListener(e->{selectedDate=today;dispose();});
        renderDays();pack();setSize(360,335);setLocationRelativeTo(owner);
    }
    LocalDate getSelectedDate(){return selectedDate;}
    private JButton navButton(String text){JButton b=new JButton(text);b.setFont(new Font("Segoe UI",Font.BOLD,20));b.setForeground(Ui.TEAL);b.setFocusPainted(false);return b;}
    private void renderDays(){
        monthLabel.setText(monthFormat.format(shownMonth));days.removeAll();
        String[] names={"S","M","T","W","T","F","S"};for(String name:names){JLabel label=new JLabel(name,SwingConstants.CENTER);label.setForeground(Ui.MUTED);label.setFont(new Font("Segoe UI",Font.BOLD,11));days.add(label);}
        int offset=shownMonth.getDayOfWeek().getValue()%7;
        for(int i=0;i<offset;i++)days.add(new JLabel(""));
        int count=shownMonth.lengthOfMonth();
        for(int day=1;day<=count;day++){
            LocalDate date=shownMonth.withDayOfMonth(day);JToggleButton cell=new JToggleButton(Integer.toString(day));cell.setFocusPainted(false);cell.setMargin(new Insets(3,3,3,3));cell.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));cell.setBackground(Color.WHITE);
            boolean past=date.isBefore(today);cell.setEnabled(!past);cell.setForeground(past?new Color(190,198,197):Ui.INK);cell.setSelected(date.equals(selectedDate));
            if(cell.isSelected()){cell.setBackground(Ui.TEAL);cell.setForeground(Color.WHITE);}else if(date.equals(today)){cell.setBorder(BorderFactory.createLineBorder(Ui.TEAL));}
            cell.addActionListener(e->{selectedDate=date;dispose();});days.add(cell);
        }
        days.revalidate();days.repaint();
    }
}
