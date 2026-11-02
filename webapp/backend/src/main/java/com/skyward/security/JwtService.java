package com.skyward.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
@Service
public class JwtService {
 private final SecretKey key;private final long ttl;
 public JwtService(@Value("${app.jwt-secret}")String secret,@Value("${app.jwt-expiration-minutes}")long ttl){key=Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));this.ttl=ttl;}
 public String issue(AppPrincipal p){Instant now=Instant.now();return Jwts.builder().subject(p.username()).claim("id",p.id()).claim("role",p.admin()?"ADMIN":"USER").claim("name",p.displayName()).issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(ttl*60))).signWith(key).compact();}
 public Claims claims(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();}
 public long ttlMinutes(){return ttl;}
}
