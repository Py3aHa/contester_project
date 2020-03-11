package com.project.SptingSecurity.Repositories;

import com.project.SptingSecurity.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepositories extends JpaRepository<Users, Long> {
    Users findByEmail(String email);
}
