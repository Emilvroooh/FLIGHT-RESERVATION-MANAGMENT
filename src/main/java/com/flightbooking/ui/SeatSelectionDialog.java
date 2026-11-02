package com.flightbooking.ui;

import com.flightbooking.model.Flight;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutionException;

final class SeatSelectionDialog extends JDialog {
    private final int requestedCount;
    private final Flight flight;
    private final Set<String> selectedSeats = new LinkedHashSet<>();
    private final Map<String,JToggleButton> seatButtons = new LinkedHashMap<>();
    private final JLabel selectionLabel = Ui.muted("");
    private final JLabel cutoffLabel = Ui.muted("");
    private final JButton continueButton = Ui.button("Continue", true);
    private final Map<JToggleButton,Color> cabinColors = new IdentityHashMap<>();
    private final Set<String> occupiedSeats = new HashSet<>();
    private final int businessCapacity;
    private boolean refreshInProgress;
    private final javax.swing.Timer seatRefreshTimer;
    private final javax.swing.Timer cutoffStatusTimer;
    private java.util.List<String> result;

    SeatSelectionDialog(AppFrame frame, Flight flight, int requestedCount) throws Exception {
        super(frame, "Choose your seats", true);
        this.requestedCount = requestedCount;
        this.flight = flight;
        this.businessCapacity = flight.getBusinessCapacity();
        occupiedSeats.addAll(frame.bookingDao().occupiedSeats(flight.getId()));
        int rows = Math.max(12, Math.min(50, (flight.getEconomyCapacity() + flight.getBusinessCapacity() + 5) / 6));

        JPanel root = new JPanel(new BorderLayout(0, 14)); root.setBackground(Ui.CANVAS);
        root.setBorder(new EmptyBorder(20, 24, 18, 24));
        JPanel heading = new JPanel(new BorderLayout(0, 5)); heading.setOpaque(false);
        heading.add(Ui.title("Choose your seats"), BorderLayout.NORTH);
        heading.add(Ui.muted("Select " + requestedCount + " seat" + (requestedCount == 1 ? "" : "s") + " for " + flight.getFlightNumber() + ". Business Class is at the front."), BorderLayout.CENTER);
        root.add(heading, BorderLayout.NORTH);

        JPanel map = new JPanel(new GridBagLayout()); map.setBackground(Ui.WHITE); map.setBorder(new EmptyBorder(14, 18, 14, 18));
        String[] letters = {"A", "B", "C", "D", "E", "F"};
        String[] positions = {"Window", "Middle", "Aisle", "Aisle", "Middle", "Window"};
        GridBagConstraints c = new GridBagConstraints(); c.gridy = 0; c.insets = new Insets(3, 5, 9, 5); c.anchor = GridBagConstraints.CENTER;
        map.add(new JLabel("ROW"), c);
        for (int i = 0; i < letters.length; i++) {
            c.gridx = i + 1; JLabel label = new JLabel("<html><center>" + letters[i] + "<br>" + positions[i] + "</center></html>", SwingConstants.CENTER);
            label.setFont(new Font("Segoe UI", Font.BOLD, 10)); label.setForeground(Ui.MUTED);
            if (i == 3) c.insets = new Insets(3, 20, 9, 5);
            map.add(label, c); c.insets = new Insets(3, 5, 9, 5);
        }
        for (int row = 1; row <= rows; row++) {
            c.gridy = row; c.gridx = 0; c.insets = new Insets(3, 5, 3, 10);
            boolean rowStartsBusiness=(row-1)*6+1<=businessCapacity, rowEndsBusiness=row*6<=businessCapacity;
            String cabinMarker=rowStartsBusiness?(rowEndsBusiness?"BUS":"B/E"):"ECO";
            JLabel rowLabel = new JLabel(String.format(Locale.ROOT, "%02d  %s", row,cabinMarker));
            rowLabel.setForeground(rowStartsBusiness?new Color(79,70,150):Ui.ACCENT);rowLabel.setFont(new Font("Segoe UI",Font.BOLD,10));
            rowLabel.setToolTipText(rowStartsBusiness?(rowEndsBusiness?"Business Class row":"Mixed cabin row"):"Economy row");map.add(rowLabel, c);
            for (int col = 0; col < letters.length; col++) {
                String seat = row + letters[col];
                int seatOrdinal=(row-1)*6+col+1;
                boolean business=seatOrdinal<=flight.getBusinessCapacity();
                boolean inCapacity=seatOrdinal<=flight.getBusinessCapacity()+flight.getEconomyCapacity();
                JToggleButton button = new JToggleButton(letters[col]); button.setPreferredSize(new Dimension(48, 34));
                button.setFont(new Font("Segoe UI", Font.BOLD, 12)); button.setFocusPainted(false);
                button.setToolTipText(seat + " · " + (business?"Business Class":"Economy") + " · "+positions[col]); button.setOpaque(true);
                c.gridx = col + 1; c.insets = col == 3 ? new Insets(3, 20, 3, 5) : new Insets(3, 5, 3, 5);
                if (!inCapacity) {
                    button.setEnabled(false); button.setBackground(new Color(229, 231, 235)); button.setForeground(new Color(107, 114, 128));
                    button.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));
                    button.setToolTipText(seat + " · " +(business?"Business Class":"Economy")+" · Unavailable");
                } else {
                    Color cabinColor=business?new Color(79,70,150):Ui.ACCENT;
                    button.setBackground(cabinColor); button.setForeground(Ui.WHITE);
                    button.setBorder(BorderFactory.createLineBorder(cabinColor));
                    seatButtons.put(seat,button);
                    cabinColors.put(button,cabinColor);
                    button.addActionListener(e -> toggleSeat(button, seat));
                }
                map.add(button, c);
            }
        }
        JScrollPane scroll = new JScrollPane(map, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(BorderFactory.createLineBorder(Ui.INPUT_BORDER)); scroll.getVerticalScrollBar().setUnitIncrement(18);
        root.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(12, 0)); footer.setOpaque(false);
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0)); legend.setOpaque(false);
        legend.add(legendItem(new Color(79,70,150), "Business · ₹"+flight.getBusinessPrice())); legend.add(legendItem(Ui.ACCENT, "Economy · ₹"+flight.getEconomyPrice())); legend.add(legendItem(new Color(229, 231, 235), "Occupied")); legend.add(legendItem(new Color(245, 158, 11), "Selected"));
        JPanel selection = new JPanel(); selection.setOpaque(false); selection.setLayout(new BoxLayout(selection,BoxLayout.Y_AXIS));selection.add(selectionLabel);selection.add(cutoffLabel);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); controls.setOpaque(false);
        JButton cancel = Ui.button("Cancel", false); controls.add(cancel); controls.add(continueButton);
        JPanel bottom = new JPanel(new BorderLayout()); bottom.setOpaque(false); bottom.add(selection, BorderLayout.WEST); bottom.add(controls, BorderLayout.EAST);
        footer.add(legend, BorderLayout.NORTH); footer.add(bottom, BorderLayout.SOUTH); root.add(footer, BorderLayout.SOUTH);
        cancel.addActionListener(e -> dispose());
        continueButton.setEnabled(false); continueButton.addActionListener(e -> {if(selectedSeats.size()==requestedCount){result=new ArrayList<>(selectedSeats);dispose();}});
        updateState();
        setContentPane(root); setMinimumSize(new Dimension(620, 520)); setSize(720, 650); setLocationRelativeTo(frame);
        seatRefreshTimer=new javax.swing.Timer(2500,e->refreshOccupiedSeats(frame,flight.getId()));
        seatRefreshTimer.setInitialDelay(2500);
        cutoffStatusTimer=new javax.swing.Timer(1000,e->updateState());
        addWindowListener(new WindowAdapter(){@Override public void windowClosed(WindowEvent e){seatRefreshTimer.stop();cutoffStatusTimer.stop();}@Override public void windowClosing(WindowEvent e){seatRefreshTimer.stop();cutoffStatusTimer.stop();}});
        seatRefreshTimer.start();
        cutoffStatusTimer.start();
    }

    java.util.List<String> getSelectedSeats() { return result; }

    String cabinClassForSeat(String seat) { return isBusiness(seat) ? "BUSINESS" : "ECONOMY"; }

    private boolean isBusiness(String seat){int row=Integer.parseInt(seat.substring(0,seat.length()-1));int col=seat.charAt(seat.length()-1)-'A';return (row-1)*6+col+1<=businessCapacity;}

    private void toggleSeat(JToggleButton button, String seat) {
        if (button.isSelected()) selectedSeats.add(seat); else selectedSeats.remove(seat);
        updateState();
    }

    private void updateState() {
        boolean bookingOpen=com.flightbooking.service.FlightSchedule.bookingOpen(flight,java.time.LocalDateTime.now());
        for (Map.Entry<String,JToggleButton> entry : seatButtons.entrySet()) {
            String seat=entry.getKey();JToggleButton button=entry.getValue();
            if(occupiedSeats.contains(seat)){
                if(button.isSelected()){button.setSelected(false);selectedSeats.remove(seat);}
                button.setEnabled(false);button.setBackground(new Color(229,231,235));button.setForeground(new Color(107,114,128));button.setBorder(BorderFactory.createLineBorder(new Color(209,213,219)));
                button.setToolTipText(seat+" · "+(isBusiness(seat)?"Business Class":"Economy")+" · Occupied");
                continue;
            }
            boolean selected = button.isSelected();
            Color cabinColor=cabinColors.getOrDefault(button,Ui.ACCENT);
            button.setBackground(selected ? new Color(245, 158, 11) : cabinColor);
            button.setForeground(Ui.WHITE);
            button.setBorder(BorderFactory.createLineBorder(selected ? new Color(217, 119, 6) : cabinColor, selected ? 2 : 1));
            button.setEnabled(bookingOpen && (selected || selectedSeats.size() < requestedCount));
            button.setToolTipText(seat+" · "+(isBusiness(seat)?"Business Class":"Economy")+(selected?" · Selected":" · Available"));
        }
        selectionLabel.setText("Selected " + selectedSeats.size() + " of " + requestedCount +
                (selectedSeats.isEmpty() ? "" : " · " + String.join(", ", selectedSeats)));
        continueButton.setEnabled(bookingOpen && selectedSeats.size() == requestedCount);
        cutoffLabel.setText(bookingOpen?"":com.flightbooking.service.FlightSchedule.message(flight,java.time.LocalDateTime.now()));
    }

    /** Polls the database so seat maps already open in other app instances converge quickly. */
    private void refreshOccupiedSeats(AppFrame frame,long flightId){
        if(refreshInProgress||!isDisplayable())return;
        refreshInProgress=true;
        new SwingWorker<Set<String>,Void>(){
            @Override protected Set<String> doInBackground() throws Exception{return frame.bookingDao().occupiedSeats(flightId);}
            @Override protected void done(){
                refreshInProgress=false;
                try{occupiedSeats.clear();occupiedSeats.addAll(get());updateState();}
                catch(InterruptedException ex){Thread.currentThread().interrupt();}
                catch(ExecutionException ignored){/* Keep the last successful view; the next poll retries. */}
            }
        }.execute();
    }

    private JPanel legendItem(Color color, String label) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0)); item.setOpaque(false);
        JPanel swatch = new JPanel(); swatch.setBackground(color); swatch.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));
        swatch.setPreferredSize(new Dimension(15, 15)); item.add(swatch); item.add(Ui.muted(label)); return item;
    }
}
