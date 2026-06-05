package com.auth.controller;

import com.auth.dto.in.UserRequest;
import com.auth.dto.out.UserResponse;
import com.auth.service.SvcUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "Usuarios", description = "Registro y consulta de usuarios")
public class CtrlUser {

    @Autowired private SvcUser svcUser;

    // @spec AUTH-REG-001, AUTH-REG-002, AUTH-REG-003, AUTH-REG-004, AUTH-REG-005
    @Operation(summary = "Registrar usuario")
    @PostMapping
    public String create(@Valid @RequestBody UserRequest request) {
        return svcUser.createUser(request);
    }

    // @spec AUTH-LST-001, AUTH-LST-002, AUTH-LST-003
    @Operation(summary = "Listar usuarios")
    @GetMapping
    public List<UserResponse> getUsers() {
        return svcUser.getUsers();
    }
}