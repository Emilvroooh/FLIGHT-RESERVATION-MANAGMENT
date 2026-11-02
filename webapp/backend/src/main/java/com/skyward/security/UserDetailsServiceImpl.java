package com.skyward.security;
import com.skyward.repository.AdminRepository;
import com.skyward.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
 private final UserRepository users;private final AdminRepository admins;public UserDetailsServiceImpl(UserRepository users,AdminRepository admins){this.users=users;this.admins=admins;}
 @Override public UserDetails loadUserByUsername(String login){
  var admin=admins.findByUsernameIgnoreCase(login);if(admin.isPresent()){var a=admin.get();return new AppPrincipal(a.getId(),a.getUsername(),a.getPasswordHash(),true,a.getUsername());}
  var user=users.findByEmailIgnoreCase(login);if(user.isPresent()){var u=user.get();return new AppPrincipal(u.getId(),u.getEmail(),u.getPasswordHash(),false,u.getName());}
  throw new UsernameNotFoundException("Account not found");
 }
}
