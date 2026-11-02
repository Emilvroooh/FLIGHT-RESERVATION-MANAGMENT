package com.skyward.security;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.util.Base64;
/** New writes use BCrypt; desktop PBKDF2 and BCrypt hashes remain verifiable during migration. */
public final class LegacyPasswordEncoder implements PasswordEncoder {
 private final BCryptPasswordEncoder bcrypt=new BCryptPasswordEncoder(12);
 @Override public String encode(CharSequence raw){return bcrypt.encode(raw);}
 @Override public boolean matches(CharSequence raw,String encoded){
  if(encoded==null)return false;if(encoded.startsWith("$2a$")||encoded.startsWith("$2b$")||encoded.startsWith("$2y$"))return bcrypt.matches(raw,encoded);
  try{String[] p=encoded.split("\\$");if(p.length!=4||!p[0].equals("PBKDF2"))return false;int n=Integer.parseInt(p[1]);byte[] salt=Base64.getDecoder().decode(p[2]),expected=Base64.getDecoder().decode(p[3]);PBEKeySpec spec=new PBEKeySpec(raw.toString().toCharArray(),salt,n,256);try{return MessageDigest.isEqual(expected,SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded());}finally{spec.clearPassword();}}catch(Exception ex){return false;}
 }
 @Override public boolean upgradeEncoding(String encoded){return encoded==null||!encoded.startsWith("$2");}
}
