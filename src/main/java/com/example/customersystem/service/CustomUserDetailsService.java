package com.example.customersystem.service;

import com.example.customersystem.model.User;
import com.example.customersystem.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.ArrayList;

@Service("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    public CustomUserDetailsService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        // ลองหาจาก Username ก่อน
        User user = userRepo.findByUsername(input);
        
        // ถ้าไม่เจอค่อยลองหาจาก Email
        if (user == null) {
            user = userRepo.findByEmail(input);
        }

        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + input);
        }

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(), 
            user.getPassword(), 
            new ArrayList<>() // ยังไม่มี roles/authorities
        );
    }
}
