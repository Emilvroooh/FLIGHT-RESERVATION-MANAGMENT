package com.flightbooking.ui;

import com.flightbooking.model.BoardingPass;
import com.flightbooking.model.Account;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDateTime;

final class BoardingPassDialog extends JDialog {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("EEE, d MMM yyyy · HH:mm");
    private final JPanel card = new JPanel(new BorderLayout(0, 16));
    private final AppFrame frame;
    private final Account account;
    private final JComboBox<BoardingPass> passPicker;
    private final JLabel journeyStatus = new JLabel();

    BoardingPassDialog(AppFrame frame, Account account, List<BoardingPass> passes) {
        super(frame, "Your Boarding Pass", Dialog.ModalityType.APPLICATION_MODAL);
        this.frame=frame;this.account=account;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        JPanel root = new JPanel(new BorderLayout(0, 14)); root.setBackground(Ui.CANVAS);
        root.setBorder(new EmptyBorder(20, 22, 20, 22));
        JPanel top = new JPanel(new BorderLayout(0, 5)); top.setOpaque(false);
        top.add(Ui.title("Your Boarding Pass"), BorderLayout.NORTH);
        top.add(Ui.muted("Your confirmed trips and assigned seats."), BorderLayout.CENTER);
        root.add(top, BorderLayout.NORTH);

        passPicker = new JComboBox<>(passes.toArray(BoardingPass[]::new));
        passPicker.setFont(Ui.BODY); passPicker.setBackground(Ui.WHITE);
        JPanel selector = new JPanel(new BorderLayout(8, 0)); selector.setOpaque(false);
        selector.add(Ui.formField("Boarding pass", passPicker), BorderLayout.CENTER);
        card.setBackground(Ui.WHITE); card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Ui.INPUT_BORDER), new EmptyBorder(22, 24, 22, 24)));
        JPanel center = new JPanel(new BorderLayout(0, 12)); center.setOpaque(false);
        center.add(selector, BorderLayout.NORTH); center.add(card, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); actions.setOpaque(false);
        JButton requestCancel = Ui.button("Request cancellation", false), close = Ui.button("Close", false);
        actions.add(requestCancel);actions.add(close); close.addActionListener(e -> dispose());
        requestCancel.addActionListener(e -> requestBookingCancellation());
        root.add(actions, BorderLayout.SOUTH);
        passPicker.addActionListener(e -> showPass((BoardingPass) passPicker.getSelectedItem()));
        setContentPane(root); setMinimumSize(new Dimension(580, 530)); setSize(700, 640); setLocationRelativeTo(frame);
        if (!passes.isEmpty()) showPass(passes.get(0));
        javax.swing.Timer statusTimer=new javax.swing.Timer(1000,e->updateJourneyStatus((BoardingPass)passPicker.getSelectedItem()));statusTimer.start();
        addWindowListener(new java.awt.event.WindowAdapter(){@Override public void windowClosed(java.awt.event.WindowEvent e){statusTimer.stop();}@Override public void windowClosing(java.awt.event.WindowEvent e){statusTimer.stop();}});
    }

    private void requestBookingCancellation(){
        BoardingPass selected=(BoardingPass)passPicker.getSelectedItem();if(selected==null)return;
        if(JOptionPane.showConfirmDialog(this,"Send a cancellation request for booking SB-"+selected.getBookingId()+"? The booking and seat stay active until an administrator approves the request.","Request cancellation",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
        try{frame.bookingDao().requestCancellation(selected.getBookingId(),account.getId());dispose();Ui.info(frame,"Your request was sent. The booking will only be cancelled after admin approval.");}
        catch(Exception ex){Ui.error(this,"Could not send the cancellation request.\n"+ex.getMessage());}
    }

    private void showPass(BoardingPass pass) {
        card.removeAll();
        if (pass == null) { card.revalidate(); card.repaint(); return; }

        JPanel banner = new JPanel(new BorderLayout(10, 0)); banner.setBackground(Ui.DARK);
        banner.setBorder(new EmptyBorder(17, 20, 17, 20));
        JPanel route = new JPanel(); route.setOpaque(false); route.setLayout(new BoxLayout(route, BoxLayout.Y_AXIS));
        JLabel logo = new JLabel("SKYWARD  ·  " + pass.getAirline()); logo.setFont(new Font("Segoe UI", Font.BOLD, 11)); logo.setForeground(new Color(209, 213, 219));
        JLabel destination = new JLabel("To " + pass.getDestinationCity()); destination.setFont(new Font("Segoe UI", Font.BOLD, 24)); destination.setForeground(Ui.WHITE);
        route.add(logo); route.add(Box.createVerticalStrut(7)); route.add(destination);
        journeyStatus.setOpaque(true);journeyStatus.setBorder(new EmptyBorder(4,8,4,8));route.add(Box.createVerticalStrut(8));route.add(journeyStatus);updateJourneyStatus(pass);
        JLabel seat = new JLabel("<html><center>SEAT<br>" + pass.getSeatNumber() + "</center></html>", SwingConstants.CENTER);
        seat.setFont(new Font("Segoe UI", Font.BOLD, 18)); seat.setForeground(Ui.WHITE);
        seat.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(107, 114, 128)), new EmptyBorder(8, 14, 8, 14)));
        banner.add(route, BorderLayout.CENTER); banner.add(seat, BorderLayout.EAST); card.add(banner, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(0, 2, 12, 13)); details.setOpaque(false);
        details.add(detail("PASSENGER NAME", pass.getPassengerName()));
        details.add(detail("BOOKING REFERENCE", "SB-" + pass.getBookingId()));
        details.add(detail("AGE / GENDER", pass.getAge() + " / " + pass.getGender()));
        String verificationId = pass.getVerificationIdType() == null
                ? blankDash(pass.getPassportNumber())
                : blankDash(pass.getVerificationIdType()) + " · ending " + maskedSuffix(pass.getVerificationIdNumber());
        details.add(detail("GOVERNMENT VERIFICATION ID", verificationId));
        details.add(detail("FLIGHT NUMBER", pass.getFlightNumber()));
        details.add(detail("TICKET NUMBER", pass.getTicketNumber()));
        details.add(detail("CABIN CLASS", "BUSINESS".equals(pass.getCabinClass()) ? "Business Class" : "Economy"));
        details.add(detail("DEPARTURE", TIME.format(pass.getDepartureTime())));
        details.add(detail("ARRIVAL", TIME.format(pass.getArrivalTime())));
        details.add(detail("TICKET PRICE", "₹" + pass.getTicketPrice()));
        JScrollPane scroll = new JScrollPane(details, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getViewport().setBackground(Ui.WHITE);
        card.add(scroll, BorderLayout.CENTER);
        card.revalidate(); card.repaint();
    }

    private void updateJourneyStatus(BoardingPass pass){if(pass==null)return;String status=com.flightbooking.service.FlightSchedule.label(pass.getDepartureTime(),pass.getArrivalTime(),LocalDateTime.now());journeyStatus.setText(status);if("Successfully Landed".equals(status)){journeyStatus.setBackground(new Color(204,251,241));journeyStatus.setForeground(new Color(15,118,110));}else if("Already Departed".equals(status)){journeyStatus.setBackground(new Color(229,231,235));journeyStatus.setForeground(new Color(75,85,99));}else if("Closing Soon".equals(status)){journeyStatus.setBackground(new Color(254,226,226));journeyStatus.setForeground(new Color(185,28,28));}else{journeyStatus.setBackground(new Color(224,242,254));journeyStatus.setForeground(new Color(3,105,161));}journeyStatus.setFont(new Font("Segoe UI",Font.BOLD,11));}

    private JPanel detail(String label, String value) {
        JPanel field = new JPanel(); field.setOpaque(false); field.setLayout(new BoxLayout(field, BoxLayout.Y_AXIS));
        JLabel caption = Ui.muted(label); caption.setFont(new Font("Segoe UI", Font.BOLD, 10));
        JLabel text = new JLabel(value); text.setFont(new Font("Segoe UI", Font.BOLD, 13)); text.setForeground(Ui.INK);
        field.add(caption); field.add(Box.createVerticalStrut(5)); field.add(text); return field;
    }

    private String blankDash(String value) { return value == null || value.isBlank() ? "—" : value; }
    private String maskedSuffix(String value) { return value == null || value.length() < 4 ? "••••" : "•••• " + value.substring(value.length() - 4); }
}
