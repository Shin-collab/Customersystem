package com.example.customersystem.repository;

import com.example.customersystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // สำหรับเช็คตอน Login
    User findByUsername(String username);

    // สำหรับเช็คตอนสมัครสมาชิก หรือลืมรหัสผ่าน
    User findByEmail(String email);
    
    // สำหรับระบบ Reset Password
    User findByResetPasswordToken(String token);
}
