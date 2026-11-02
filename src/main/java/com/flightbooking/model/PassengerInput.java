package com.flightbooking.model;
public class PassengerInput {
    private final String name,gender,verificationIdType,verificationIdNumber,seatNumber,cabinClass;
    private final int age;
    public PassengerInput(String name,int age,String gender,String verificationIdType,String verificationIdNumber){
        this(name,age,gender,verificationIdType,verificationIdNumber,null,null);
    }
    public PassengerInput(String name,int age,String gender,String verificationIdType,String verificationIdNumber,String seatNumber,String cabinClass){
        this.name=name;this.age=age;this.gender=gender;this.verificationIdType=verificationIdType;
        this.verificationIdNumber=verificationIdNumber;this.seatNumber=seatNumber;this.cabinClass=cabinClass;
    }
    public String getName(){return name;} public int getAge(){return age;} public String getGender(){return gender;}
    public String getVerificationIdType(){return verificationIdType;} public String getVerificationIdNumber(){return verificationIdNumber;}
    /** Kept for older integrations: the value is populated only for Passport IDs. */
    public String getPassportNo(){return "Passport".equals(verificationIdType)?verificationIdNumber:null;}
    public String getSeatNumber(){return seatNumber;} public String getCabinClass(){return cabinClass;}
}
