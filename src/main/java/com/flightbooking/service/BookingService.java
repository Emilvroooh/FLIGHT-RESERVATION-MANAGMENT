package com.flightbooking.service;

import com.flightbooking.dao.BookingDao;
import com.flightbooking.model.PassengerInput;
import com.flightbooking.model.TicketInfo;
import java.sql.SQLException;
import java.util.*;

public class BookingService {
    private final BookingDao bookings;
    public BookingService(BookingDao bookings){this.bookings=bookings;}
    public List<PassengerInput> validatePassengers(List<PassengerInput> passengers) {
        if(passengers==null)throw new IllegalArgumentException("Add at least one passenger.");
        List<PassengerInput> list=new ArrayList<>();
        for(PassengerInput passenger:passengers){
            if(passenger==null)throw new IllegalArgumentException("Complete every passenger row.");
            String name=passenger.getName()==null?"":passenger.getName().trim();
            String gender=passenger.getGender()==null?"":passenger.getGender().trim();
            String passport=passenger.getPassportNo()==null?"":passenger.getPassportNo().trim();
            int age=passenger.getAge();
            if(name.isBlank()||name.length()>120||age<0||age>120||!Set.of("Male","Female","Other").contains(gender)||passport.length()>32)
                throw new IllegalArgumentException("Check each passenger's name, age (0–120), gender, and passport number (32 characters maximum).");
            String cabin=passenger.getCabinClass()==null?null:passenger.getCabinClass().toUpperCase(Locale.ROOT);
            list.add(new PassengerInput(name,age,gender,passport.isBlank()?null:passport,passenger.getSeatNumber(),cabin));
        }
        if(list.isEmpty()||list.size()>8)throw new IllegalArgumentException("Enter between 1 and 8 passengers.");
        return list;
    }
    public long create(long userId,long flightId,List<PassengerInput> passengers) throws SQLException {
        List<PassengerInput> list=validatePassengers(passengers);
        Set<String> selected=new HashSet<>();
        for(PassengerInput passenger:list){String seat=passenger.getSeatNumber();if(seat==null||!seat.matches("[1-9][0-9]{0,2}[A-F]")||!selected.add(seat))throw new IllegalArgumentException("Choose a different seat for each passenger.");}
        return bookings.create(userId,flightId,list);
    }
    public List<TicketInfo> pay(long bookingId,long userId,String method) throws SQLException {if(!Set.of("CARD","UPI","NET_BANKING").contains(method))throw new IllegalArgumentException("Choose a valid payment method.");return bookings.payAndIssueTickets(bookingId,userId,method);}
}
