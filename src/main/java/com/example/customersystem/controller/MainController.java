package com.example.customersystem.controller;

import com.example.customersystem.model.Customer;
import com.example.customersystem.repository.CustomerRepository;
import com.example.customersystem.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;
import java.util.List;

@Controller
public class MainController {

    private final CustomerRepository customerRepo;
    private final UserRepository userRepo;

    public MainController(CustomerRepository customerRepo, UserRepository userRepo) {
        this.customerRepo = customerRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/")
    public String index(Model model, Principal p) {
        if (p != null) {
            String username = p.getName();
        
            List<Customer> list = customerRepo.findByCreatedBy(username);
            model.addAttribute("customers", list);
            
            model.addAttribute("user", userRepo.findByUsername(username));
        }
        return "index";
    }

    @GetMapping("/profile")
    public String viewProfile(Model model, Principal p) {
        if (p != null) {
            String name = p.getName();
            model.addAttribute("user", userRepo.findByUsername(name));

            
            model.addAttribute("customers", customerRepo.findByCreatedBy(name));
        }
        return "profile";
    }
}
