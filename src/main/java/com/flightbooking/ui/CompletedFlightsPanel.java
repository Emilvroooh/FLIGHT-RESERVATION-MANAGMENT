package com.flightbooking.ui;

import com.flightbooking.model.Flight;
import com.flightbooking.service.FlightSchedule;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/** Admin view for departed/landed operations and their historical passenger manifests. */
final class CompletedFlightsPanel extends JPanel {
    private final AppFrame frame;
    private final JTable table=Ui.table("Flight","Airline","Route","Departure","Arrival","Status","Seats E/B");
    private List<Flight> pastFlights=List.of();
    private final javax.swing.Timer refreshTimer;

    CompletedFlightsPanel(AppFrame frame){this.frame=frame;setLayout(new BorderLayout(0,12));setBackground(Ui.PAPER);setBorder(new javax.swing.border.EmptyBorder(18,22,22,22));JPanel top=new JPanel(new BorderLayout());top.setOpaque(false);top.add(Ui.title("Departed and completed flights"),BorderLayout.WEST);JPanel actions=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0));actions.setOpaque(false);JButton refresh=Ui.button("Refresh",false),manifest=Ui.button("View passenger manifest",true);actions.add(refresh);actions.add(manifest);top.add(actions,BorderLayout.EAST);add(top,BorderLayout.NORTH);add(new JScrollPane(table),BorderLayout.CENTER);refresh.addActionListener(e->reload());manifest.addActionListener(e->showManifest());reload();refreshTimer=new javax.swing.Timer(10_000,e->reload());refreshTimer.start();}
    private void reload(){try{LocalDateTime now=LocalDateTime.now();pastFlights=frame.flightDao().listAll().stream().filter(f->!now.isBefore(f.getDepartureTime())).toList();DefaultTableModel m=Ui.model(table);m.setRowCount(0);DateTimeFormatter dt=DateTimeFormatter.ofPattern("d MMM yyyy · HH:mm");for(Flight f:pastFlights)m.addRow(new Object[]{f.getFlightNumber(),f.getAirlineName(),f.getDepartureCity()+" → "+f.getDestinationCity(),dt.format(f.getDepartureTime()),dt.format(f.getArrivalTime()),FlightSchedule.label(f.getDepartureTime(),f.getArrivalTime(),now),f.getEconomyAvailableSeats()+" / "+f.getBusinessAvailableSeats()});}catch(Exception ex){Ui.error(this,"Could not load completed flights.\n"+ex.getMessage());}}
    private void showManifest(){int row=table.getSelectedRow();if(row<0){Ui.info(this,"Select a departed flight first.");return;}Flight f=pastFlights.get(table.convertRowIndexToModel(row));PassengerManifestPanel report=new PassengerManifestPanel(frame);report.showFlight(f.getId());JDialog dialog=new JDialog(SwingUtilities.getWindowAncestor(this),"Passenger Manifest · "+f.getFlightNumber(),Dialog.ModalityType.APPLICATION_MODAL);dialog.setContentPane(report);dialog.setSize(1120,650);dialog.setLocationRelativeTo(this);dialog.setVisible(true);}
    @Override public void addNotify(){super.addNotify();if(refreshTimer!=null)refreshTimer.start();}
    @Override public void removeNotify(){if(refreshTimer!=null)refreshTimer.stop();super.removeNotify();}
}
