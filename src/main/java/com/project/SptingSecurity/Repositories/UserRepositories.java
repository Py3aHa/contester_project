package com.project.SptingSecurity.Repositories;

import com.project.SptingSecurity.entities.Roles;
import com.project.SptingSecurity.entities.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepositories extends JpaRepository<Users, Long> {
    Users findByEmail(String email);
    List<Users> findByRolesOrderById(Roles role);

}
