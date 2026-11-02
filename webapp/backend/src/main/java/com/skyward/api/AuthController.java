package com.skyward.api;
import com.skyward.api.ApiDtos.*;
import com.skyward.entity.AdminEntity;
import com.skyward.entity.PasswordResetTokenEntity;
import com.skyward.repository.AdminRepository;
import com.skyward.repository.PasswordResetTokenRepository;
import com.skyward.repository.UserRepository;
import com.skyward.security.AppPrincipal;
import com.skyward.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Base64;
import java.util.Map;

@RestController @RequestMapping("/api/auth")
public class AuthController {
 private final UserRepository users;private final AdminRepository admins;private final PasswordResetTokenRepository resetTokens;private final PasswordEncoder passwords;private final AuthenticationManager auth;private final JwtService jwt;private final ObjectProvider<JavaMailSender> mail;private final String adminPasskey,frontendUrl;private final SecureRandom random=new SecureRandom();
 public AuthController(UserRepository users,AdminRepository admins,PasswordResetTokenRepository resetTokens,PasswordEncoder passwords,AuthenticationManager auth,JwtService jwt,ObjectProvider<JavaMailSender> mail,@Value("${app.admin-passkey}")String adminPasskey,@Value("${app.frontend-url}")String frontendUrl){this.users=users;this.admins=admins;this.resetTokens=resetTokens;this.passwords=passwords;this.auth=auth;this.jwt=jwt;this.mail=mail;this.adminPasskey=adminPasskey;this.frontendUrl=frontendUrl;}
 @PostMapping("/register") public Map<String,String> register(@Valid @RequestBody Register r){String email=r.email().trim().toLowerCase(Locale.ROOT);if(users.existsByEmailIgnoreCase(email))throw new IllegalArgumentException("That email already has an account.");users.save(new com.skyward.entity.UserEntity(r.name().trim(),email,r.phone(),passwords.encode(r.password())));return Map.of("message","Account created. Sign in with your email.");}
 @PostMapping("/admin/register") public Map<String,String> registerAdmin(@Valid @RequestBody AdminRegister r){if(!safeEquals(adminPasskey,r.adminPasskey()))throw new SecurityException("Invalid Admin Passkey");admins.save(new AdminEntity(r.username().trim(),passwords.encode(r.password())));return Map.of("message","Administrator account created.");}
 @PostMapping("/login") public LoginResult login(@Valid @RequestBody Login r){
  Authentication result=auth.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(r.login().trim(),r.password()));AppPrincipal p=(AppPrincipal)result.getPrincipal();
  if(p.admin()&&!safeEquals(adminPasskey,r.adminPasskey()))throw new SecurityException("Invalid Admin Passkey");
  if(passwords.upgradeEncoding(p.password())){String hash=passwords.encode(r.password());if(p.admin())admins.findById(p.id()).ifPresent(a->{a.setPasswordHash(hash);admins.save(a);});else users.findById(p.id()).ifPresent(u->{u.setPasswordHash(hash);users.save(u);});}
  return new LoginResult(jwt.issue(p),jwt.ttlMinutes(),p.id(),p.displayName(),p.username(),p.admin()?"ADMIN":"USER");
 }
 @GetMapping("/me") public Map<String,Object> me(org.springframework.security.core.Authentication a){AppPrincipal p=(AppPrincipal)a.getPrincipal();return Map.of("id",p.id(),"name",p.displayName(),"login",p.username(),"role",p.admin()?"ADMIN":"USER");}
 @Transactional @PostMapping("/password-reset/request") public Map<String,String> requestReset(@Valid @RequestBody ResetRequest r){
  var user=users.findByEmailIgnoreCase(r.email().trim());
  if(user.isPresent())try{byte[] bytes=new byte[32];random.nextBytes(bytes);String token=Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);String hash=sha256(token);resetTokens.save(new PasswordResetTokenEntity(user.get(),hash,LocalDateTime.now().plusMinutes(15)));JavaMailSender sender=mail.getIfAvailable();if(sender!=null){SimpleMailMessage message=new SimpleMailMessage();message.setTo(r.email());message.setSubject("Skyward password reset");message.setText("Use this one-time link within 15 minutes: "+frontendUrl+"/reset-password?token="+token);sender.send(message);}}
  catch(Exception ignored){/* Keep the response generic so account existence is not exposed. */}
  return Map.of("message","If the address belongs to an account, a password reset link will be sent.");
 }
 @Transactional @PostMapping("/password-reset/complete") public Map<String,String> completeReset(@Valid @RequestBody ResetPassword r){if(!r.newPassword().equals(r.confirmPassword()))throw new IllegalArgumentException("New passwords do not match.");String hash=sha256(r.token());PasswordResetTokenEntity token=resetTokens.lockValidToken(hash,LocalDateTime.now()).orElseThrow(()->new IllegalArgumentException("Reset link is invalid or expired."));var user=token.getUser();user.setPasswordHash(passwords.encode(r.newPassword()));users.save(user);token.markUsed(LocalDateTime.now());resetTokens.save(token);return Map.of("message","Password updated. Sign in with the new password.");}
 private boolean safeEquals(String expected,String supplied){if(supplied==null)return false;return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),supplied.getBytes(StandardCharsets.UTF_8));}
 private String sha256(String value){try{return Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}
