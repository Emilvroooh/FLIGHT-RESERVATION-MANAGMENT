package com.flightbooking.dao;

import com.flightbooking.config.DatabaseConnection;
import com.flightbooking.model.Account;
import com.flightbooking.model.UserInfo;
import com.flightbooking.security.PasswordUtil;
import java.sql.*;
import java.util.*;

public class AccountDao {
    public Account authenticate(String login,char[] password) throws SQLException {
        String value=login.trim();
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT admin_id,username,password_hash FROM admins WHERE LOWER(username)=LOWER(?)")){
            p.setString(1,value);try(ResultSet r=p.executeQuery()){if(r.next()){long id=r.getLong("admin_id");String username=r.getString("username"),encoded=r.getString("password_hash");if(PasswordUtil.verify(password,encoded)){upgradeAdminHash(c,id,encoded,password);return new Account(id,username,username,true);}}}
        }
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT user_id,name,email,password_hash FROM users WHERE LOWER(email)=LOWER(?)")){
            p.setString(1,value);try(ResultSet r=p.executeQuery()){if(r.next()){long id=r.getLong("user_id");String name=r.getString("name"),email=r.getString("email"),encoded=r.getString("password_hash");if(PasswordUtil.verify(password,encoded)){upgradeUserHash(c,id,encoded,password);return new Account(id,name,email,false);}}}
        }
        return null;
    }
    private void upgradeAdminHash(Connection c,long id,String oldHash,char[] password)throws SQLException{if(!PasswordUtil.needsUpgrade(oldHash))return;try(PreparedStatement p=c.prepareStatement("UPDATE admins SET password_hash=? WHERE admin_id=?")){p.setString(1,PasswordUtil.hash(password));p.setLong(2,id);p.executeUpdate();}}
    private void upgradeUserHash(Connection c,long id,String oldHash,char[] password)throws SQLException{if(!PasswordUtil.needsUpgrade(oldHash))return;try(PreparedStatement p=c.prepareStatement("UPDATE users SET password_hash=? WHERE user_id=?")){p.setString(1,PasswordUtil.hash(password));p.setLong(2,id);p.executeUpdate();}}
    public void register(String name,String email,String phone,char[] password) throws SQLException {
        String sql="INSERT INTO users(name,email,phone,password_hash) VALUES(?,?,?,?)";
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            p.setString(1,name.trim());p.setString(2,email.trim().toLowerCase(Locale.ROOT));p.setString(3,phone==null||phone.isBlank()?null:phone.trim());p.setString(4,PasswordUtil.hash(password));p.executeUpdate();
        } catch(SQLIntegrityConstraintViolationException ex){throw new SQLException("An account with that email already exists.",ex);}
    }
    public int adminCount() throws SQLException {try(Connection c=DatabaseConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM admins")){r.next();return r.getInt(1);}}
    public void createAdmin(String username,char[] password) throws SQLException {
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO admins(username,password_hash) VALUES(?,?)")){p.setString(1,username.trim());p.setString(2,PasswordUtil.hash(password));p.executeUpdate();}
    }
    public List<UserInfo> listUsers() throws SQLException {
        List<UserInfo> out=new ArrayList<>();try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT user_id,name,email,phone FROM users ORDER BY name,email");ResultSet r=p.executeQuery()){while(r.next())out.add(new UserInfo(r.getLong(1),r.getString(2),r.getString(3),r.getString(4)));}return out;
    }
    public void resetUserPassword(String email,char[] password) throws SQLException {
        try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE users SET password_hash=? WHERE LOWER(email)=LOWER(?)")){
            p.setString(1,PasswordUtil.hash(password));p.setString(2,email);if(p.executeUpdate()!=1)throw new SQLException("Passenger account was not found.");
        }
    }
    public boolean userExists(String email) throws SQLException {try(Connection c=DatabaseConnection.getConnection();PreparedStatement p=c.prepareStatement("SELECT 1 FROM users WHERE LOWER(email)=LOWER(?)")){p.setString(1,email);try(ResultSet r=p.executeQuery()){return r.next();}}}
}
