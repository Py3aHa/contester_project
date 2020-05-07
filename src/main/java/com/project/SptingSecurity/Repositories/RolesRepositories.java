package com.project.SptingSecurity.Repositories;

import com.project.SptingSecurity.entities.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepositories extends JpaRepository<Roles, Long> {
    Optional<Roles> findById(Long id);
}
