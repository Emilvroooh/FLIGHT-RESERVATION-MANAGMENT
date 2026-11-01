package com.flightbooking.ui;

import com.flightbooking.model.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

final class BookingDialog extends JDialog {
    private final List<PassengerRow> passengerRows = new ArrayList<>();
    private final JPanel rowsPanel = new JPanel();
    private final JLabel countLabel = Ui.muted("1 of 8 passengers");
    private final AppFrame frame;
    private final Account account;
    private final Flight flight;

    BookingDialog(AppFrame frame, Account account, Flight flight) {
        super(frame, "Passenger details", true);
        this.frame = frame; this.account = account; this.flight = flight;
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(820, 620)); setSize(980, 720); setLocationRelativeTo(frame);

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Ui.CANVAS); root.setBorder(new EmptyBorder(22, 26, 20, 26));
        JPanel header = new JPanel(new BorderLayout(0, 5)); header.setOpaque(false);
        header.add(Ui.title("Passenger details"), BorderLayout.NORTH);
        header.add(Ui.muted("Enter details for each traveler. Add up to 8 passengers."), BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        rowsPanel.setOpaque(false); rowsPanel.setLayout(new BoxLayout(rowsPanel, BoxLayout.Y_AXIS));
        JPanel form = new JPanel(new BorderLayout(0, 10)); form.setOpaque(false);
        JPanel formHeader = new JPanel(new BorderLayout()); formHeader.setOpaque(false);
        JButton addPassenger = Ui.button("+ Add Passenger", false);
        addPassenger.addActionListener(e -> addPassengerRow());
        formHeader.add(countLabel, BorderLayout.WEST); formHeader.add(addPassenger, BorderLayout.EAST);
        form.add(formHeader, BorderLayout.NORTH);
        JScrollPane passengerScroll = new JScrollPane(rowsPanel);
        passengerScroll.setBorder(BorderFactory.createEmptyBorder());
        passengerScroll.getViewport().setBackground(Ui.CANVAS);
        passengerScroll.getVerticalScrollBar().setUnitIncrement(18);
        form.add(passengerScroll, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 14)); center.setOpaque(false);
        center.add(form, BorderLayout.CENTER); center.add(routeSummary(), BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); actions.setOpaque(false);
        JButton cancel = Ui.button("Cancel", false), reserve = Ui.button("Reserve seats", true);
        actions.add(cancel); actions.add(reserve); root.add(actions, BorderLayout.SOUTH);
        cancel.addActionListener(e -> dispose());
        reserve.addActionListener(e -> reserveBooking());
        addPassengerRow();
        setContentPane(root);
    }

    private void addPassengerRow() {
        if (passengerRows.size() >= 8) { Ui.info(this, "A booking can include up to 8 passengers."); return; }
        PassengerRow row = new PassengerRow();
        passengerRows.add(row); rowsPanel.add(row.panel);
        refreshRows();
    }

    private void removePassengerRow(PassengerRow row) {
        if (passengerRows.size() <= 1) { Ui.info(this, "At least one passenger is required."); return; }
        passengerRows.remove(row); rowsPanel.remove(row.panel); refreshRows();
    }

    private void refreshRows() {
        countLabel.setText(passengerRows.size() + " of 8 passengers");
        for (PassengerRow row : passengerRows) row.remove.setEnabled(passengerRows.size() > 1);
        rowsPanel.revalidate(); rowsPanel.repaint();
    }

    private void reserveBooking() {
        try {
            List<PassengerInput> passengers = new ArrayList<>();
            for (PassengerRow row : passengerRows) passengers.add(row.toInput());
            passengers = frame.bookingService().validatePassengers(passengers);
            SeatSelectionDialog seatDialog = new SeatSelectionDialog(frame, flight, passengers.size());
            seatDialog.setVisible(true);
            List<String> selectedSeats = seatDialog.getSelectedSeats();
            if (selectedSeats == null) return;
            List<PassengerInput> seatedPassengers = new ArrayList<>();
            for (int i = 0; i < passengers.size(); i++) {
                PassengerInput passenger = passengers.get(i);
                String seat=selectedSeats.get(i);
                seatedPassengers.add(new PassengerInput(passenger.getName(), passenger.getAge(), passenger.getGender(), passenger.getPassportNo(), seat, seatDialog.cabinClassForSeat(seat)));
            }
            long id = frame.bookingService().create(account.getId(), flight.getId(), seatedPassengers);
            dispose();
            java.math.BigDecimal total=java.math.BigDecimal.ZERO;
            for(PassengerInput passenger:seatedPassengers)total=total.add("BUSINESS".equals(passenger.getCabinClass())?flight.getBusinessPrice():flight.getEconomyPrice());
            String method = (String) JOptionPane.showInputDialog(frame,
                    "Booking SB-" + id + " is pending. Simulate payment for ₹" + total + "?\nNo payment details are collected.",
                    "Demo payment", JOptionPane.QUESTION_MESSAGE, null, new String[]{"CARD", "UPI", "NET_BANKING"}, "CARD");
            if (method != null) {
                List<TicketInfo> tickets = frame.bookingService().pay(id, account.getId(), method);
                StringBuilder msg = new StringBuilder("Confirmed · SB-").append(id).append("\n\n");
                for (TicketInfo ticket : tickets) msg.append(ticket.getPassengerName()).append(" · Seat ").append(ticket.getSeatNumber()).append(" · ").append(ticket.getTicketNumber()).append('\n');
                Ui.info(frame, msg.toString());
            } else Ui.info(frame, "Seats are reserved. Continue payment from My bookings.");
        } catch (Exception ex) {
            String message=ex.getMessage()==null?"Booking could not be created.":ex.getMessage();
            if(message.startsWith("Seat ")&&message.endsWith("is already taken, please choose another.")){
                Ui.error(this,message+" The seat map has been refreshed; select another seat.");
                SwingUtilities.invokeLater(this::reserveBooking);
            }else Ui.error(this, "Booking could not be created.\n" + message);
        }
    }

    private JPanel routeSummary() {
        JPanel route = new JPanel(new GridLayout(2, 3, 10, 8));
        route.setBackground(Ui.WHITE); route.setBorder(new EmptyBorder(13, 16, 13, 16));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE, d MMM yyyy · HH:mm");
        route.add(info(flight.getAirlineName(), "AIRLINE")); route.add(info(flight.getFlightNumber(), "FLIGHT"));
        route.add(info(fmt.format(flight.getDepartureTime()), "DEPARTURE"));
        route.add(info(flight.getDepartureCity() + " → " + flight.getDestinationCity(), "ROUTE"));
        route.add(info("Economy ₹" + flight.getEconomyPrice() + " · Business ₹" + flight.getBusinessPrice(), "FARES"));
        route.add(info("Economy " + flight.getEconomyAvailableSeats() + " · Business " + flight.getBusinessAvailableSeats(), "AVAILABLE"));
        return route;
    }

    private JPanel info(String value, String label) {
        JPanel panel = new JPanel(); panel.setOpaque(false); panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel cap = Ui.muted(label); cap.setFont(new Font("Segoe UI", Font.BOLD, 10));
        JLabel val = new JLabel(value); val.setForeground(Ui.INK); val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(cap); panel.add(Box.createVerticalStrut(4)); panel.add(val); return panel;
    }

    private final class PassengerRow {
        final JTextField name = Ui.field(15);
        final JSpinner age = new JSpinner(new SpinnerNumberModel(18, 0, 120, 1));
        final JComboBox<String> gender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        final JTextField passport = Ui.field(13);
        final JButton remove = Ui.button("Remove", false);
        final JPanel panel = new JPanel(new GridBagLayout());

        PassengerRow() {
            panel.setBackground(Ui.WHITE);
            panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Ui.INPUT_BORDER), new EmptyBorder(12, 12, 12, 12)));
            age.setFont(Ui.BODY); age.setBorder(BorderFactory.createLineBorder(Ui.INPUT_BORDER));
            gender.setFont(Ui.BODY); gender.setBackground(Ui.WHITE);
            GridBagConstraints c = new GridBagConstraints(); c.gridy = 0; c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST; c.weightx = 1; c.insets = new Insets(0, 0, 0, 8);
            addField("Full Name", name, 0, c); addField("Age", age, 1, c); addField("Gender", gender, 2, c); addField("Passport Number", passport, 3, c);
            c.gridx = 4; c.weightx = 0; c.insets = new Insets(20, 0, 0, 0); panel.add(remove, c);
            remove.addActionListener(e -> removePassengerRow(this));
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        }

        private void addField(String label, JComponent field, int x, GridBagConstraints base) {
            GridBagConstraints c = (GridBagConstraints) base.clone(); c.gridx = x; c.weightx = x == 0 ? 1.5 : 1;
            panel.add(Ui.formField(label, field), c);
        }

        PassengerInput toInput() {
            return new PassengerInput(name.getText(), (Integer) age.getValue(), (String) gender.getSelectedItem(), passport.getText());
        }
    }
}
