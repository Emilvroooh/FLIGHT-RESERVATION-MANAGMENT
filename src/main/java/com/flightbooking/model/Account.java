package com.flightbooking.model;

public class Account {
    private final long id; private final String name; private final String login; private final boolean admin;
    public Account(long id,String name,String login,boolean admin){this.id=id;this.name=name;this.login=login;this.admin=admin;}
    public long getId(){return id;} public String getName(){return name;} public String getLogin(){return login;} public boolean isAdmin(){return admin;}
}
