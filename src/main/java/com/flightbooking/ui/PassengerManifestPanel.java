package com.flightbooking.ui;

import com.flightbooking.model.Flight;
import com.flightbooking.model.PassengerManifestEntry;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/** Admin-only active ticket manifest; IDs are masked unless explicitly revealed. */
final class PassengerManifestPanel extends JPanel {
    private final AppFrame frame;
    private final JComboBox<Flight> flightChoice = new JComboBox<>();
    private final JCheckBox showFullIds = new JCheckBox("Show full verification ID numbers");
    private final JLabel status = Ui.muted("Select a flight and load its active passenger manifest.");
    private final JTable table = Ui.table("Booking Ref","Passenger","Age","Gender","ID Type","Verification ID","Seat","Cabin");
    private List<PassengerManifestEntry> rows = List.of();

    PassengerManifestPanel(AppFrame frame) {
        this.frame=frame; setLayout(new BorderLayout(0,12)); setBackground(Ui.PAPER); setBorder(new javax.swing.border.EmptyBorder(16,18,18,18));
        JPanel controls=new JPanel(new FlowLayout(FlowLayout.LEFT,9,3)); controls.setOpaque(false);
        JButton refresh=Ui.button("Refresh flights",false), load=Ui.button("Load manifest",true), export=Ui.button("Export CSV",false);
        controls.add(Ui.formField("Flight (upcoming, departed, or landed)",flightChoice)); controls.add(refresh); controls.add(load); controls.add(export); controls.add(showFullIds);
        add(controls,BorderLayout.NORTH); JPanel center=new JPanel(new BorderLayout(0,8));center.setOpaque(false);center.add(status,BorderLayout.NORTH);center.add(new JScrollPane(table),BorderLayout.CENTER);add(center,BorderLayout.CENTER);
        refresh.addActionListener(e->loadFlights());load.addActionListener(e->loadManifest());export.addActionListener(e->exportCsv());showFullIds.addActionListener(e->render());
        loadFlights();
    }
    private void loadFlights(){try{flightChoice.removeAllItems();for(Flight f:frame.flightDao().listAll())flightChoice.addItem(f);status.setText(flightChoice.getItemCount()==0?"No flights are available.":"Choose any flight to view its manifest. Verification IDs are masked by default.");}catch(Exception ex){Ui.error(this,"Could not load flights.\n"+ex.getMessage());}}
    void showFlight(long flightId){for(int i=0;i<flightChoice.getItemCount();i++)if(flightChoice.getItemAt(i).getId()==flightId){flightChoice.setSelectedIndex(i);loadManifest();return;}}
    private void loadManifest(){Flight f=(Flight)flightChoice.getSelectedItem();if(f==null){Ui.info(this,"Select a flight first.");return;}try{rows=frame.bookingDao().passengerManifest(f.getId());render();status.setText(f.getFlightNumber()+" · "+f.getDepartureCity()+" → "+f.getDestinationCity()+" · "+rows.size()+" active passenger(s) · generated "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM yyyy HH:mm")));}catch(Exception ex){Ui.error(this,"Could not generate the passenger manifest.\n"+ex.getMessage());}}
    private void render(){DefaultTableModel m=Ui.model(table);m.setRowCount(0);for(PassengerManifestEntry r:rows)m.addRow(new Object[]{"SB-"+r.bookingId(),r.name(),r.age(),r.gender(),r.verificationIdType()==null?"—":r.verificationIdType(),showFullIds.isSelected()?safe(r.verificationIdNumber()):r.maskedVerificationId(),r.seatNumber(),r.cabinClass()});}
    private void exportCsv(){if(rows.isEmpty()){Ui.info(this,"Load a manifest before exporting.");return;}JFileChooser chooser=new JFileChooser();chooser.setSelectedFile(new File("passenger-manifest.csv"));if(chooser.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION)return;try(BufferedWriter out=java.nio.file.Files.newBufferedWriter(chooser.getSelectedFile().toPath(),StandardCharsets.UTF_8)){out.write("Booking Reference,Full Name,Age,Gender,Verification ID Type,Verification ID,Seat Number,Cabin Class\r\n");for(PassengerManifestEntry r:rows){String[] values={"SB-"+r.bookingId(),r.name(),String.valueOf(r.age()),r.gender(),safe(r.verificationIdType()),showFullIds.isSelected()?safe(r.verificationIdNumber()):r.maskedVerificationId(),r.seatNumber(),r.cabinClass()};for(int i=0;i<values.length;i++){if(i>0)out.write(',');out.write(csv(values[i]));}out.write("\r\n");}Ui.info(this,"Manifest exported. Verification IDs were "+(showFullIds.isSelected()?"included in full":"masked")+".");}catch(IOException ex){Ui.error(this,"Could not export the manifest.\n"+ex.getMessage());}}
    private String safe(String value){return value==null?"":value;}
    private String csv(String value){return "\""+safe(value).replace("\"","\"\"")+"\"";}
}
