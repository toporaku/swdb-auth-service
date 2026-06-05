package com.auth.controller;

import com.auth.dto.in.LoginRequest;
import com.auth.entity.User;
import com.auth.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@Tag(name = "Autenticación", description = "Login y obtención de JWT")
public class CtrlAuth {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtUtil jwtUtil;

    // @spec AUTH-LOG-001, AUTH-LOG-002, AUTH-LOG-003, AUTH-LOG-004
    @Operation(summary = "Login")
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()
                )
        );
        User user   = (User) auth.getPrincipal();
        String token = jwtUtil.generateToken(user);
        return Map.of("token", token);
    }
}