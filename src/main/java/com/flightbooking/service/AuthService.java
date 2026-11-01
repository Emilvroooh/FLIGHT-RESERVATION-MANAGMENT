package com.flightbooking.service;

import com.flightbooking.dao.AccountDao;
import com.flightbooking.model.Account;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class AuthService {
    private static final String ADMIN_PASSKEY = "20080301";
    private static final Pattern EMAIL=Pattern.compile("^[A-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",Pattern.CASE_INSENSITIVE);
    private final AccountDao accounts;
    public AuthService(AccountDao accounts){this.accounts=accounts;}
    /** Admins are returned only when the per-login passkey also matches. */
    public Account login(String login,char[] password) throws SQLException {return login(login,password,null);}
    public Account login(String login,char[] password,char[] adminPasskey) throws SQLException {
        if(login==null||login.isBlank()||password==null||password.length==0)return null;
        Account account=accounts.authenticate(login,password);
        if(account!=null&&account.isAdmin()&&!matchesAdminPasskey(adminPasskey))throw new SecurityException("Invalid Admin Passkey");
        return account;
    }
    public void createAdmin(String username,char[] password,char[] adminPasskey) throws SQLException {
        if(!matchesAdminPasskey(adminPasskey))throw new SecurityException("Invalid Admin Passkey");
        if(username==null||username.isBlank()||username.trim().length()>80)throw new IllegalArgumentException("Enter an administrator username (up to 80 characters).");
        if(password==null||password.length<8||password.length>100)throw new IllegalArgumentException("Administrator password must be between 8 and 100 characters.");
        if(accounts.adminCount()!=0)throw new IllegalArgumentException("An administrator already exists.");
        accounts.createAdmin(username,password);
    }
    private static boolean matchesAdminPasskey(char[] candidate){
        if(candidate==null)return false;
        int difference=candidate.length^ADMIN_PASSKEY.length();
        int length=Math.max(candidate.length,ADMIN_PASSKEY.length());
        for(int i=0;i<length;i++)difference|=(i<candidate.length?candidate[i]:0)^(i<ADMIN_PASSKEY.length()?ADMIN_PASSKEY.charAt(i):0);
        return difference==0;
    }
    public void register(String name,String email,String phone,char[] password) throws SQLException {
        if(name==null||name.isBlank()||name.trim().length()>120)throw new IllegalArgumentException("Enter a name (up to 120 characters).");
        if(email==null||email.trim().length()>254||!EMAIL.matcher(email.trim()).matches())throw new IllegalArgumentException("Enter a valid email address.");
        if(phone!=null&&phone.length()>32)throw new IllegalArgumentException("Phone number must be 32 characters or fewer.");
        if(password==null||password.length<8||password.length>100)throw new IllegalArgumentException("Password must be between 8 and 100 characters.");
        if(accounts.userExists(email.trim()))throw new IllegalArgumentException("That email already has an account. Sign in or use Forgot password.");
        accounts.register(name,email,phone,password);
    }
    public void resetPassengerPassword(String email,char[] password,char[] confirmation) throws SQLException {
        if(email==null||email.isBlank()||!EMAIL.matcher(email.trim()).matches())throw new IllegalArgumentException("Enter the passenger account email address.");
        if(password==null||password.length<8||password.length>100)throw new IllegalArgumentException("Password must be between 8 and 100 characters.");
        if(confirmation==null||!java.util.Arrays.equals(password,confirmation))throw new IllegalArgumentException("The new passwords do not match.");
        if(!accounts.userExists(email.trim()))throw new IllegalArgumentException("No passenger account was found for that email address.");
        accounts.resetUserPassword(email.trim(),password);
    }
}
