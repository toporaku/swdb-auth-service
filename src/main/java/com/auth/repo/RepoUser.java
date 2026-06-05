package com.auth.repo;

import com.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepoUser extends JpaRepository<User, Long> {

    //Buscar por username
    Optional<User> findByUsername(String username);
}
