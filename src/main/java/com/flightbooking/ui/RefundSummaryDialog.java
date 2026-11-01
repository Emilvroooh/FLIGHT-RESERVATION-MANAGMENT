package com.flightbooking.ui;

import com.flightbooking.model.RefundSummary;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

final class RefundSummaryDialog extends JDialog {
    RefundSummaryDialog(Window owner, RefundSummary refund) {
        super(owner, "Refund Summary", Dialog.ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(0, 16)); root.setBackground(Ui.CANVAS);
        root.setBorder(new EmptyBorder(22, 26, 20, 26));
        JPanel header = new JPanel(new BorderLayout(0, 5)); header.setOpaque(false);
        header.add(Ui.title("Refund Summary"), BorderLayout.NORTH);
        header.add(Ui.muted("Booking SB-" + refund.getBookingId() + " · " + refund.getFlightNumber()), BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        JPanel card = new JPanel(new GridLayout(0, 2, 12, 14)); card.setBackground(Ui.WHITE);
        card.setBorder(new EmptyBorder(20, 22, 20, 22));
        card.add(detail("FLIGHT ROUTE", refund.getDepartureCity() + " → " + refund.getDestinationCity()));
        card.add(detail("FLIGHT NUMBER", refund.getFlightNumber()));
        card.add(detail("ORIGINAL TICKET PRICE", "₹" + refund.getOriginalAmount()));
        card.add(detail("TIME BEFORE DEPARTURE", durationText(refund.getSecondsBeforeDeparture())));
        card.add(detail("REFUND TIER", refund.getRefundPercentage().stripTrailingZeros().toPlainString() + "%"));
        card.add(detail("AMOUNT CREDITED", "₹" + refund.getRefundedAmount()));
        JPanel reason = detail("WHY THIS REFUND APPLIES", refund.getReason());
        card.add(reason); card.add(detail("DEPARTURE", DateTimeFormatter.ofPattern("EEE, d MMM yyyy · HH:mm").format(refund.getDepartureTime())));
        root.add(card, BorderLayout.CENTER);

        JLabel note = Ui.muted("This demo records the refund in the database; it does not contact a payment provider.");
        JButton close = Ui.button("Done", true); close.addActionListener(e -> dispose());
        JPanel footer = new JPanel(new BorderLayout()); footer.setOpaque(false); footer.add(note, BorderLayout.WEST);
        JPanel action = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); action.setOpaque(false); action.add(close); footer.add(action, BorderLayout.EAST);
        root.add(footer, BorderLayout.SOUTH);
        setContentPane(root); setMinimumSize(new Dimension(560, 380)); setSize(700, 450); setLocationRelativeTo(owner);
        getRootPane().setDefaultButton(close);
    }

    private JPanel detail(String label, String value) {
        JPanel panel = new JPanel(); panel.setOpaque(false); panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel caption = Ui.muted(label); caption.setFont(new Font("Segoe UI", Font.BOLD, 10));
        JLabel text = new JLabel("<html>" + value + "</html>"); text.setFont(new Font("Segoe UI", Font.BOLD, 13)); text.setForeground(Ui.INK);
        panel.add(caption); panel.add(Box.createVerticalStrut(5)); panel.add(text); return panel;
    }

    private String durationText(long seconds) {
        Duration d = Duration.ofSeconds(seconds);
        long days=d.toDays(),hours=d.toHoursPart(),minutes=d.toMinutesPart(),remaining=d.toSecondsPart();
        return (days>0?days+" days, ":"")+hours+" hours, "+minutes+" minutes, "+remaining+" seconds";
    }
}
