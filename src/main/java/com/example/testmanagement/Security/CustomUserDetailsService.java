package com.example.testmanagement.Security;


import com.example.testmanagement.Entities.User;
import com.example.testmanagement.Repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService{

    private final UserRepository repo;

    public CustomUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repo.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        System.out.println("roles : " + user.getRoles().toString());

        return new CustomUserDetails(user);
    }
}
