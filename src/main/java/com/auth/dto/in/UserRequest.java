package com.auth.dto.in;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "El username es obligatorio")
    private String username;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@_$#!%*?&]).{8,}$",
            message = "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial (@$!%*?&)"
    )
    private String password;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String lastName;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(
            regexp = "^[0-9]{10,15}$",
            message = "El teléfono debe tener entre 10 y 15 dígitos numéricos"
    )
    private String phoneNumber;


}
