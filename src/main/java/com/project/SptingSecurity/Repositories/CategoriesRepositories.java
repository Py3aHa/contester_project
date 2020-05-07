package com.project.SptingSecurity.Repositories;

import com.project.SptingSecurity.entities.TaskCategories;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriesRepositories extends JpaRepository<TaskCategories, Long> {
    Optional<TaskCategories> findByCategory(String name);
    Optional<TaskCategories> findById(Long id);
    List<TaskCategories> findAllByOrderByCategory();

}
