package com.auth.dto.out;

import com.auth.entity.User;
import lombok.Data;

import java.util.Set;

@Data
public class UserResponse {
    private Long        id;
    private String      username;
    private String      email;
    private String      name;
    private String      lastname;
    private String      phoneNumber;
    private Set<String> roles;

    public UserResponse(User user) {
        this.id       = user.getId();
        this.username = user.getUsername();
        this.email    = user.getEmail();
        this.name = user.getName();
        this.lastname  = user.getLastName();
        this.phoneNumber = user.getPhoneNumber();
        this.roles    = user.getRoles();
    }
}
