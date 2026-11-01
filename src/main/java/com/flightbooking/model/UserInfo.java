package com.flightbooking.model;
public class UserInfo {
    private final long id;
    private final String name,email,phone;
    public UserInfo(long id,String name,String email,String phone){this.id=id;this.name=name;this.email=email;this.phone=phone;}
    public long getId(){return id;}
    public String getName(){return name;} public String getEmail(){return email;} public String getPhone(){return phone;}
    @Override public String toString(){return name+"  ·  "+email;}
}
