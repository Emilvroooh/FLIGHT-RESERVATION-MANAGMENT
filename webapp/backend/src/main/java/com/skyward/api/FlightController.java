package com.skyward.api;

import com.skyward.entity.FlightEntity;
import com.skyward.repository.AirlineRepository;
import com.skyward.repository.FlightRepository;
import com.skyward.repository.TicketRepository;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/flights")
public class FlightController {
 private final FlightRepository flights;
 private final AirlineRepository airlines;
 private final TicketRepository tickets;
 public FlightController(FlightRepository flights,AirlineRepository airlines,TicketRepository tickets){this.flights=flights;this.airlines=airlines;this.tickets=tickets;}

 @GetMapping public List<Map<String,Object>> search(@RequestParam(required=false)String from,@RequestParam(required=false)String to,@RequestParam(required=false)LocalDate date,@RequestParam(required=false)BigDecimal maxPrice){
  if(maxPrice!=null&&maxPrice.signum()<0)throw new IllegalArgumentException("Maximum fare cannot be negative.");
  var start=date==null?null:date.atStartOfDay();var end=date==null?null:date.plusDays(1).atStartOfDay();
  return flights.search(blankToNull(from),blankToNull(to),start,end,maxPrice,java.time.LocalDateTime.now()).stream().map(this::flightCard).filter(f->((Number)f.get("economyAvailable")).intValue()+((Number)f.get("businessAvailable")).intValue()>0).toList();
 }
 @GetMapping("/cities") public Map<String,List<String>> cities(){Set<String> names=new TreeSet<>();for(FlightEntity f:flights.findAll()){names.add(f.getDepartureCity());names.add(f.getDestinationCity());}return Map.of("cities",new ArrayList<>(names));}
 @GetMapping("/airlines") public List<Map<String,Object>> airlines(){return airlines.findAll().stream().map(a->Map.<String,Object>of("airlineId",a.getId(),"airlineName",a.getName())).toList();}
 @GetMapping("/{flightId}/seats") public List<Map<String,Object>> seats(@PathVariable long flightId){
  FlightEntity flight=flights.findById(flightId).orElseThrow(()->new IllegalArgumentException("Flight was not found."));Set<String> occupied=new HashSet<>(tickets.findActiveSeats(flightId));int total=flight.getEconomyCapacity()+flight.getBusinessCapacity();String[] positions={"Window","Middle","Aisle","Aisle","Middle","Window"};List<Map<String,Object>> result=new ArrayList<>();
  for(int ordinal=1;ordinal<=total;ordinal++){int row=(ordinal-1)/6+1,col=(ordinal-1)%6;String seat=row+String.valueOf((char)('A'+col));Map<String,Object> item=new LinkedHashMap<>();item.put("seatNumber",seat);item.put("row",row);item.put("letter",String.valueOf((char)('A'+col)));item.put("position",positions[col]);item.put("cabinClass",ordinal<=flight.getBusinessCapacity()?"BUSINESS":"ECONOMY");item.put("status",occupied.contains(seat)?"OCCUPIED":"AVAILABLE");result.add(item);}return result;
 }
 private Map<String,Object> flightCard(FlightEntity f){Map<String,Integer> occupied=new HashMap<>();for(Object[] row:tickets.countActiveSeatsByCabin(f.getId()))occupied.put((String)row[0],((Number)row[1]).intValue());int econ=Math.max(0,f.getEconomyCapacity()-occupied.getOrDefault("ECONOMY",0)),biz=Math.max(0,f.getBusinessCapacity()-occupied.getOrDefault("BUSINESS",0));java.time.LocalDateTime now=java.time.LocalDateTime.now(),cutoff=f.getDepartureTime().minusMinutes(30);String status=!now.isBefore(f.getArrivalTime())?"LANDED":!now.isBefore(f.getDepartureTime())?"DEPARTED":!now.isBefore(cutoff)?"CLOSING_SOON":"SCHEDULED";boolean open=now.isBefore(f.getDepartureTime())&&!now.isAfter(cutoff);Map<String,Object> m=new LinkedHashMap<>();m.put("flightId",f.getId());m.put("airlineId",f.getAirline().getId());m.put("airlineName",f.getAirline().getName());m.put("flightNumber",f.getFlightNumber());m.put("departureCity",f.getDepartureCity());m.put("destinationCity",f.getDestinationCity());m.put("departureTime",f.getDepartureTime());m.put("arrivalTime",f.getArrivalTime());m.put("flightStatus",status);m.put("bookingOpen",open);m.put("economyCapacity",f.getEconomyCapacity());m.put("businessCapacity",f.getBusinessCapacity());m.put("economyPrice",f.getEconomyPrice());m.put("businessPrice",f.getBusinessPrice());m.put("economyAvailable",econ);m.put("businessAvailable",biz);return m;}
 private String blankToNull(String v){return v==null||v.isBlank()?null:v.trim();}
}
