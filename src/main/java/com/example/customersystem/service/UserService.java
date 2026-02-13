package com.example.customersystem.service;

import com.example.customersystem.model.User;
import com.example.customersystem.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepo, BCryptPasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional 
    public void saveUser(User user) {
        
        // ตรวจสอบชื่อซ้ำ (Validation)
        User existingUser = userRepo.findByUsername(user.getUsername());
        if (existingUser != null && !existingUser.getId().equals(user.getId())) {
            throw new RuntimeException("Username นี้มีคนใช้แล้ว");
        }

        // ตรวจสอบอีเมลซ้ำ
        User existingEmail = userRepo.findByEmail(user.getEmail());
        if (existingEmail != null && !existingEmail.getId().equals(user.getId())) {
            throw new RuntimeException("Email นี้มีคนใช้แล้ว");
        }

        // เข้ารหัสผ่านเฉพาะกรณีที่เป็นรหัสใหม่ (ยังไม่โดน Hash)
        if (user.getPassword() != null && user.getPassword().length() < 30) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userRepo.save(user); 
    }

    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Transactional 
    public boolean updatePassword(String email, String oldPass, String newPass) {
        User user = userRepo.findByEmail(email);
        if (user != null && passwordEncoder.matches(oldPass, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPass));
            userRepo.save(user);
            return true;
        }
        return false;
    }
}
