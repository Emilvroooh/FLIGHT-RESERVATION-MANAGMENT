package com.skyward.security;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
public record AppPrincipal(long id,String username,String password,boolean admin,String displayName) implements UserDetails {
 @Override public Collection<? extends GrantedAuthority> getAuthorities(){return List.of(new SimpleGrantedAuthority(admin?"ROLE_ADMIN":"ROLE_USER"));}
 @Override public String getPassword(){return password;} @Override public String getUsername(){return username;}
 @Override public boolean isAccountNonExpired(){return true;} @Override public boolean isAccountNonLocked(){return true;} @Override public boolean isCredentialsNonExpired(){return true;} @Override public boolean isEnabled(){return true;}
}
