package com.project.SptingSecurity.Repositories;

import com.project.SptingSecurity.entities.Lessons;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface LessonsRepositories extends JpaRepository<Lessons, Long> {
    int countAllByDeletedAtNull();
    Optional<Lessons> findByIdAndDeletedAtNull(Long id);
    List<Lessons> findAllByDeletedAtNullOrderByPostDateDesc(Pageable pageable);
}
