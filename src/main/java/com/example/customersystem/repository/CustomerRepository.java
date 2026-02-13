package com.example.customersystem.repository;

import com.example.customersystem.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // ดึงรายชื่อลูกค้าตามชื่อผู้สร้าง (Username)
    List<Customer> findByCreatedBy(String createdBy);
    
}
