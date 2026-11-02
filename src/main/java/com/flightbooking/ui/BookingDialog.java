package com.flightbooking.ui;

import com.flightbooking.model.*;
import com.flightbooking.service.VerificationIdValidator;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

final class BookingDialog extends JDialog {
    private final List<PassengerRow> passengerRows = new ArrayList<>();
    private final JPanel rowsPanel = new JPanel();
    private final JLabel countLabel = Ui.muted("1 of 8 passengers");
    private final AppFrame frame;
    private final Account account;
    private final Flight flight;
    private final JButton reserveButton = Ui.button("Reserve seats", true);
    private final JLabel cutoffLabel = Ui.muted("");

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

        JPanel actions = new JPanel(new BorderLayout()); actions.setOpaque(false);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); buttons.setOpaque(false);
        JButton cancel = Ui.button("Cancel", false); buttons.add(cancel); buttons.add(reserveButton);
        actions.add(cutoffLabel, BorderLayout.WEST); actions.add(buttons, BorderLayout.EAST); root.add(actions, BorderLayout.SOUTH);
        cancel.addActionListener(e -> dispose());
        reserveButton.addActionListener(e -> reserveBooking());
        addPassengerRow();
        setContentPane(root);
        javax.swing.Timer cutoffTimer = new javax.swing.Timer(1000, e -> refreshCutoffState());
        cutoffTimer.start();
        addWindowListener(new java.awt.event.WindowAdapter(){@Override public void windowClosed(java.awt.event.WindowEvent e){cutoffTimer.stop();}@Override public void windowClosing(java.awt.event.WindowEvent e){cutoffTimer.stop();}});
        refreshCutoffState();
    }

    private void refreshCutoffState(){boolean open=com.flightbooking.service.FlightSchedule.bookingOpen(flight,java.time.LocalDateTime.now());reserveButton.setEnabled(open);cutoffLabel.setText(open?"":"   "+com.flightbooking.service.FlightSchedule.message(flight,java.time.LocalDateTime.now()));}

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
            if (!com.flightbooking.service.FlightSchedule.bookingOpen(flight, java.time.LocalDateTime.now())) {
                Ui.info(this, com.flightbooking.service.FlightSchedule.message(flight, java.time.LocalDateTime.now()));
                dispose();
                return;
            }
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
                seatedPassengers.add(new PassengerInput(passenger.getName(), passenger.getAge(), passenger.getGender(), passenger.getVerificationIdType(), passenger.getVerificationIdNumber(), seat, seatDialog.cabinClassForSeat(seat)));
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
        final JComboBox<String> verificationType = new JComboBox<>(new String[]{
                "Select ID type", VerificationIdValidator.PAN, VerificationIdValidator.AADHAAR, VerificationIdValidator.PASSPORT});
        final JTextField verificationNumber = Ui.field(18);
        final JLabel formatHint = Ui.muted("");
        final JLabel validationMessage = new JLabel(" ");
        final JButton remove = Ui.button("Remove", false);
        final JPanel panel = new JPanel(new GridBagLayout());
        private boolean verificationTouched;

        PassengerRow() {
            panel.setBackground(Ui.WHITE);
            panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Ui.INPUT_BORDER), new EmptyBorder(12, 12, 12, 12)));
            age.setFont(Ui.BODY); age.setBorder(BorderFactory.createLineBorder(Ui.INPUT_BORDER));
            gender.setFont(Ui.BODY); gender.setBackground(Ui.WHITE);
            verificationType.setFont(Ui.BODY); verificationType.setBackground(Ui.WHITE);
            validationMessage.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            GridBagConstraints c = new GridBagConstraints(); c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST; c.insets = new Insets(0, 0, 8, 8);
            addField("Full Name", name, 0, 0, 1.7, c);
            addField("Age", age, 1, 0, .65, c);
            addField("Gender", gender, 2, 0, 1, c);
            c.gridx = 3; c.gridy = 0; c.weightx = 0; c.insets = new Insets(20, 0, 8, 0); panel.add(remove, c);
            c.insets = new Insets(0, 0, 8, 8);
            addField("Verification ID Type", verificationType, 0, 1, 1.2, c);
            JPanel numberField = new JPanel(new BorderLayout(0, 3)); numberField.setOpaque(false);
            numberField.add(verificationNumber, BorderLayout.CENTER); numberField.add(formatHint, BorderLayout.SOUTH);
            GridBagConstraints numberConstraints = (GridBagConstraints)c.clone();
            numberConstraints.gridx = 1; numberConstraints.gridy = 1; numberConstraints.gridwidth = 3;
            numberConstraints.weightx = 2.5; numberConstraints.insets = new Insets(0, 0, 8, 0);
            panel.add(Ui.formField("Verification ID Number", numberField), numberConstraints);
            GridBagConstraints errorConstraints = (GridBagConstraints)c.clone();
            errorConstraints.gridx = 0; errorConstraints.gridy = 2; errorConstraints.gridwidth = 4;
            errorConstraints.weightx = 1; errorConstraints.insets = new Insets(-4, 0, 0, 0);
            panel.add(validationMessage, errorConstraints);
            verificationType.addActionListener(e -> updateVerificationHint());
            verificationNumber.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) { refreshValidationIfTouched(); }
                public void removeUpdate(DocumentEvent e) { refreshValidationIfTouched(); }
                public void changedUpdate(DocumentEvent e) { refreshValidationIfTouched(); }
            });
            verificationNumber.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusLost(java.awt.event.FocusEvent e) { verificationTouched = true; updateVerificationHint(); }
            });
            updateVerificationHint();
            remove.addActionListener(e -> removePassengerRow(this));
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        }

        private void addField(String label, JComponent field, int x, int y, double weight, GridBagConstraints base) {
            GridBagConstraints c = (GridBagConstraints) base.clone(); c.gridx = x; c.gridy = y; c.gridwidth = 1; c.weightx = weight;
            panel.add(Ui.formField(label, field), c);
        }

        private void refreshValidationIfTouched() { if (verificationTouched) updateVerificationHint(); }

        private void updateVerificationHint() {
            String type = (String) verificationType.getSelectedItem();
            String hint = switch (type == null ? "" : type) {
                case VerificationIdValidator.PAN -> "e.g., ABCDE1234F (10-digit alphanumeric)";
                case VerificationIdValidator.AADHAAR -> "e.g., 123456789012 (12-digit number)";
                case VerificationIdValidator.PASSPORT -> "e.g., 123456789 or A12345678 (9 characters)";
                default -> "Choose an ID type to see its required format.";
            };
            formatHint.setText(hint);
            verificationNumber.setToolTipText(hint);
            if (!verificationTouched) return;
            String number = VerificationIdValidator.normalize(verificationNumber.getText());
            String error = number.isBlank() ? "Verification ID number is required."
                    : VerificationIdValidator.isValid(type, number) ? " " : VerificationIdValidator.errorMessage(type);
            validationMessage.setText(error);
            validationMessage.setForeground(" ".equals(error) ? Ui.MUTED : new Color(185, 28, 28));
            verificationNumber.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(" ".equals(error) ? Ui.INPUT_BORDER : new Color(220, 38, 38)),
                    new EmptyBorder(9, 11, 9, 11)));
        }

        PassengerInput toInput() {
            verificationTouched = true;
            updateVerificationHint();
            return new PassengerInput(name.getText(), (Integer) age.getValue(), (String) gender.getSelectedItem(),
                    (String) verificationType.getSelectedItem(), verificationNumber.getText());
        }
    }
}
