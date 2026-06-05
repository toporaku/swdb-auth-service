package com.auth.config;

import com.auth.entity.User;
import com.auth.repo.RepoUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

// @spec AUTH-INT-003
@Component
public class AdminSeeder implements CommandLineRunner {

    @Autowired
    private RepoUser repoUser;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (repoUser.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("Password123!"));
            admin.setName("Default");
            admin.setLastName("Admin");
            admin.setPhoneNumber("9999999990");
            admin.setRoles(Set.of("Administrator"));
            repoUser.save(admin);
            System.out.println("Default Administrator user seeded successfully.");
        }
    }
}
