package com.flightbooking.security;

import org.mindrot.jbcrypt.BCrypt;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtil {
    private static final int ITERATIONS=310_000, KEY_BITS=256;
    private static final SecureRandom RANDOM=new SecureRandom();
    private PasswordUtil() {}
    public static String hash(char[] password) {
        byte[] salt=new byte[16]; RANDOM.nextBytes(salt);
        byte[] result=derive(password,salt,ITERATIONS);
        return "PBKDF2$"+ITERATIONS+"$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(result);
    }
    public static boolean verify(char[] password,String encoded) {
        try {
            if(encoded!=null&&(encoded.startsWith("$2a$")||encoded.startsWith("$2b$")||encoded.startsWith("$2y$")))
                return BCrypt.checkpw(new String(password),encoded);
            String[] parts=encoded.split("\\$"); if(parts.length!=4||!parts[0].equals("PBKDF2")) return false;
            int iterations=Integer.parseInt(parts[1]); byte[] salt=Base64.getDecoder().decode(parts[2]); byte[] expected=Base64.getDecoder().decode(parts[3]);
            return MessageDigest.isEqual(expected,derive(password,salt,iterations));
        } catch(Exception ex) { return false; }
    }
    public static boolean needsUpgrade(String encoded){return encoded!=null&&encoded.startsWith("$2");}
    private static byte[] derive(char[] password,byte[] salt,int iterations) {
        PBEKeySpec spec=new PBEKeySpec(password,salt,iterations,KEY_BITS);
        try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}
        catch(Exception ex){throw new IllegalStateException("Password hashing is unavailable.",ex);}
        finally{spec.clearPassword();}
    }
}
