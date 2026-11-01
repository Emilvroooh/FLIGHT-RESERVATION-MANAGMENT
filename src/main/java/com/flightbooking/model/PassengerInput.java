package com.flightbooking.model;
public class PassengerInput {
    private final String name,gender,passportNo,seatNumber,cabinClass; private final int age;
    public PassengerInput(String name,int age,String gender,String passportNo){this(name,age,gender,passportNo,null,null);}
    public PassengerInput(String name,int age,String gender,String passportNo,String seatNumber){this(name,age,gender,passportNo,seatNumber,null);}
    public PassengerInput(String name,int age,String gender,String passportNo,String seatNumber,String cabinClass){this.name=name;this.age=age;this.gender=gender;this.passportNo=passportNo;this.seatNumber=seatNumber;this.cabinClass=cabinClass;}
    public String getName(){return name;} public int getAge(){return age;} public String getGender(){return gender;} public String getPassportNo(){return passportNo;} public String getSeatNumber(){return seatNumber;} public String getCabinClass(){return cabinClass;}
}
