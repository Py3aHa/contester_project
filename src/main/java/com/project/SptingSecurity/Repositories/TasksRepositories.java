package com.project.SptingSecurity.Repositories;

import com.project.SptingSecurity.entities.TaskCategories;
import com.project.SptingSecurity.entities.Tasks;
import com.project.SptingSecurity.entities.Users;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TasksRepositories extends JpaRepository<Tasks, Long> {
    int countAllBy();
    int countAllByTitle(String title);
    int countAllByCategories(TaskCategories category);
    Optional<Tasks> findById(Long id);
    Optional<Tasks> findByTitleAndAuthor(String title, Users user);
    List<Tasks> findAllByCategoriesOrderByLevel(TaskCategories category, Pageable pageable);
    List<Tasks> findAllByCategoriesOrderByLevelDesc(TaskCategories category, Pageable pageable);
    List<Tasks> findAllByOrderByLevel(Pageable pageable);
    List<Tasks> findAllByOrderByLevelDesc(Pageable pageable);
    List<Tasks> findAllByTitleOrderByLevel(String title, Pageable pageable);
}
