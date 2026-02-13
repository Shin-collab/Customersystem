package com.example.customersystem.controller;

import com.example.customersystem.model.Customer;
import com.example.customersystem.repository.CustomerRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository customerRepo;

    public CustomerController(CustomerRepository customerRepo) {
        this.customerRepo = customerRepo;
    }

    @GetMapping("/add")
    public String viewAddPage(Model model) {
        model.addAttribute("customer", new Customer());
        return "add-customer";
    }

    @GetMapping("/edit/{id}")
    public String viewEditPage(@PathVariable Long id, Model model, Principal p) {
        if (id == null || p == null) return "redirect:/";

        Customer c = customerRepo.findById(id).orElse(null);
        if (c == null) return "redirect:/";

        String user = p.getName();
        if (c.getCreatedBy() == null || !c.getCreatedBy().equals(user)) {
            return "redirect:/?error=no_permission"; 
        }

        model.addAttribute("customer", c);
        return "add-customer";
    }

    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, Principal p) {
        if (id == null || p == null) return "redirect:/";

        Customer c = customerRepo.findById(id).orElse(null);
        if (c != null && p.getName().equals(c.getCreatedBy())) {
            customerRepo.deleteById(id);
        }
        return "redirect:/";
    }

    @PostMapping("/save")
    public String saveCustomer(@ModelAttribute("customer") Customer customer, Principal p) {
        if (p == null) return "redirect:/login";

        
        customer.setCreatedBy(p.getName());

        // ไม่ได้เรียนอยู่ใส่ - ให้หมด
        if (!"กำลังศึกษา".equals(customer.getOccupation())) {
            customer.setEducation("-");
            customer.setEducationYear("-");
            customer.setMajor("-");
            customer.setSchoolName("-");
        }
        
        customerRepo.save(customer);
        return "redirect:/"; 
    }
}
