package com.example.customersystem.controller;

import com.example.customersystem.model.User;
import com.example.customersystem.service.UserService;
import com.example.customersystem.service.EmailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Random;

@Controller
public class AuthController {

    private final UserService userService;
    private final EmailService emailService;

    @Autowired
    public AuthController(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    @GetMapping("/login")
    public String viewLoginPage() {
        return "login";
    }

    @RequestMapping("/login-success")
    public String handleLoginSuccess(HttpSession session, Authentication auth, @CookieValue(value = "trusted_device", defaultValue = "false") String isTrusted) {
        try {
            if (auth == null || !auth.isAuthenticated()) {
                System.out.println("login fail");
                return "redirect:/login?error";
            }
            
            if ("true".equals(isTrusted)) {
                return "redirect:/"; 
            }

            String name = auth.getName();
            User user = userService.findByUsername(name);
            
            if (user != null) {
                // generate otp
                String otp = String.format("%06d", new Random().nextInt(1000000));
                System.out.println(">>> otp: " + otp); // ไว้ดูในคอนโซล
                
                session.setAttribute("OTP_CODE", otp);
                session.setAttribute("PENDING_USER", user);
                emailService.sendOtpEmail(user.getEmail(), otp);
                
                return "redirect:/verify-otp";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/login?error";
    }

    @GetMapping("/register")
    public String viewRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(User user, Model model) {
        try {
            userService.saveUser(user);
            model.addAttribute("title", "Success");
            model.addAttribute("message", "Register success!");
            return "success"; 
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("user", user);
            return "register";
        }
    }

    @GetMapping("/verify-otp")
    public String viewVerifyOtpPage(HttpSession session) {
        if (session.getAttribute("OTP_CODE") == null) return "redirect:/login";
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String otp, HttpSession session, HttpServletResponse response, Model model) {
        String sOtp = (String) session.getAttribute("OTP_CODE");
        if (sOtp != null && sOtp.equals(otp)) {
            session.removeAttribute("OTP_CODE");
            Cookie c = new Cookie("trusted_device", "true");
            c.setMaxAge(604800); // 7 days
            c.setPath("/");
            response.addCookie(c);
            return "redirect:/";
        }
        model.addAttribute("error", "Wrong OTP");
        return "verify-otp";
    }

    @GetMapping("/forgot-password")
    public String viewForgotPasswordPage() { return "forgot-password"; }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, HttpSession session, Model model) {
        User user = userService.findByEmail(email);
        if (user != null) {
            String otp = String.format("%06d", new Random().nextInt(1000000));
            session.setAttribute("FORGOT_PASS_OTP", otp);
            session.setAttribute("FORGOT_USER_EMAIL", email);
            emailService.sendOtpEmail(email, otp);
            return "redirect:/verify-forgot-password";
        }
        model.addAttribute("error", "Email not found");
        return "forgot-password";
    }

    @GetMapping("/verify-forgot-password")
    public String viewVerifyForgotOtpPage() { return "verify-forgot-password"; }

    @PostMapping("/verify-forgot-password")
    public String verifyForgotOtp(@RequestParam String otp, HttpSession session, Model model) {
        String saved = (String) session.getAttribute("FORGOT_PASS_OTP");
        if (saved != null && saved.equals(otp)) return "redirect:/reset-password";
        model.addAttribute("error", "invalid otp");
        return "verify-forgot-password";
    }

    @GetMapping("/reset-password")
    public String viewResetPasswordPage(HttpSession session) {
        if (session.getAttribute("FORGOT_USER_EMAIL") == null) return "redirect:/login";
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam String newPassword, HttpSession session, Model model) {
        String email = (String) session.getAttribute("FORGOT_USER_EMAIL");
        User u = userService.findByEmail(email);
        if (u != null) {
            u.setPassword(newPassword);
            userService.saveUser(u);
            session.invalidate();
            model.addAttribute("title", "Done");
            model.addAttribute("message", "Password changed");
            return "success";
        }
        return "redirect:/login";
    }

    @PostMapping("/request-change-password")
    public String requestChangePassword(@RequestParam String newPassword, HttpSession session, Authentication auth) {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        session.setAttribute("CHANGE_PASS_OTP", otp);
        session.setAttribute("NEW_PASSWORD_TEMP", newPassword);
        User u = userService.findByUsername(auth.getName());
        emailService.sendOtpEmail(u.getEmail(), otp);
        return "redirect:/verify-change-password";
    }

    @GetMapping("/verify-change-password")
    public String viewVerifyChangeOtpPage() { return "verify-change-password"; }

    @PostMapping("/verify-change-password")
    public String verifyChangePassword(@RequestParam String otp, HttpSession session, Authentication auth, Model model) {
        String s = (String) session.getAttribute("CHANGE_PASS_OTP");
        if (s != null && s.equals(otp)) {
            User u = userService.findByUsername(auth.getName());
            u.setPassword((String) session.getAttribute("NEW_PASSWORD_TEMP"));
            userService.saveUser(u);
            session.removeAttribute("CHANGE_PASS_OTP");
            model.addAttribute("title", "OK");
            return "success";
        }
        model.addAttribute("error", "otp wrong");
        return "verify-change-password";
    }
}
