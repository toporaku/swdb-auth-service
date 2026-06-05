package com.auth.service;

import com.auth.dto.in.UserRequest;
import com.auth.dto.out.UserResponse;
import com.auth.entity.User;
import com.auth.repo.RepoUser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class SvcUserImp implements SvcUser {

    @Autowired
    private RepoUser repoUser;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public String createUser(UserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setName(request.getName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());
        // Encriptamos DESPUÉS de pasar las validaciones del DTO
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of("User"));
        repoUser.save(user);
        return "Usuario registrado exitosamente";
    }

    @Override
    public List<UserResponse> getUsers(){
        return repoUser.findAll().stream()
                .map(UserResponse::new)
                .collect(Collectors.toList());
    }

}
