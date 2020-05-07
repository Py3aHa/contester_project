package com.project.SptingSecurity.Repositories;

import com.project.SptingSecurity.entities.Comments;
import com.project.SptingSecurity.entities.Lessons;
import com.project.SptingSecurity.entities.NewPosts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentsRepositories extends JpaRepository<Comments, Long> {
    List<Comments> findAllByNewsPostOrderByPostDateDesc(NewPosts newPosts);
    List<Comments> findAllByNewsPost(NewPosts newPosts);

    List<Comments> findAllByLessonOrderByPostDateDesc(Lessons lessons);
    List<Comments> findAllByLesson(Lessons lessons);
}
