package com.auth.service;

import com.auth.dto.in.UserRequest;
import com.auth.dto.out.UserResponse;
import com.auth.entity.User;

import java.util.List;

public interface SvcUser {

    String createUser(UserRequest userRequest);

    List<UserResponse> getUsers();

}
