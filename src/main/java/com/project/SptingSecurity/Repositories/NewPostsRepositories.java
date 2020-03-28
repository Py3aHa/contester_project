package com.project.SptingSecurity.Repositories;

import com.project.SptingSecurity.entities.NewPosts;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NewPostsRepositories extends JpaRepository<NewPosts, Long> {
    int countAllByDeletedAtNull();
    Optional<NewPosts> findByIdAndDeletedAtNull(Long id);
    List<NewPosts> findAllByDeletedAtNullOrderByPostDateDesc(Pageable pageable);
}
