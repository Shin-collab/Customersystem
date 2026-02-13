package com.example.customersystem.controller;

import com.example.customersystem.model.User;
import com.example.customersystem.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.security.Principal;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepo;
    private final BCryptPasswordEncoder encoder;

    public UserController(UserRepository userRepo, BCryptPasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam String oldPassword, 
                                 @RequestParam String newPassword, 
                                 Principal p, RedirectAttributes ra) {
        
        User user = userRepo.findByUsername(p.getName());

        // เช็ครหัสผ่านเก่าว่าตรงกับใน DB มั้ย
        if (!encoder.matches(oldPassword, user.getPassword())) {
            ra.addFlashAttribute("error", "รหัสผ่านเดิมไม่ถูกต้อง!");
            return "redirect:/profile";
        }

        // เข้ารหัสใหม่ก่อนเซฟ
        user.setPassword(encoder.encode(newPassword));
        userRepo.save(user);
        
        ra.addFlashAttribute("success", "เปลี่ยนรหัสผ่านสำเร็จแล้ว");
        return "redirect:/profile";
    }

    @PostMapping("/update-avatar")
    public String updateAvatar(@RequestParam String avatarUrl, Principal p) {
        User user = userRepo.findByUsername(p.getName());
        user.setAvatar(avatarUrl);
        userRepo.save(user);
        
        return "redirect:/profile";
    }
}
