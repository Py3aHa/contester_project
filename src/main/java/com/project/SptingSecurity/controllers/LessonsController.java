package com.project.SptingSecurity.controllers;

import com.project.SptingSecurity.Repositories.CommentsRepositories;
import com.project.SptingSecurity.Repositories.LessonsRepositories;
import com.project.SptingSecurity.Repositories.UserRepositories;
import com.project.SptingSecurity.entities.Comments;
import com.project.SptingSecurity.entities.Lessons;
import com.project.SptingSecurity.entities.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(path = "lessons")
public class LessonsController {

    @Autowired
    UserRepositories userRepositories;

    @Autowired
    LessonsRepositories lessonsRepositories;

    @Autowired
    CommentsRepositories commentsRepositories;

    public Users getUserData(){
        Users userData = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            User secUser = (User)authentication.getPrincipal();
            userData = userRepositories.findByEmail(secUser.getUsername());
        }
        return userData;
    }

    @GetMapping(path = "lessons")
    public String LessonsPage(Model model, @RequestParam(name = "page", defaultValue = "1") int page){
        int size = lessonsRepositories.countAllByDeletedAtNull();

        int tabSize = (size+4)/10;

        if(tabSize < 1){
            tabSize = 1;
        }

        Pageable pageable = PageRequest.of(page-1,12);
        List<Lessons> lessons = lessonsRepositories.findAllByDeletedAtNullOrderByPostDateDesc(pageable);
        model.addAttribute("tabSize", tabSize);
        model.addAttribute("lessons", lessons);
        model.addAttribute("user", getUserData());
        return "lessons/lessons";
    }

    @GetMapping(path = "addNewLesson")
    @PreAuthorize("hasAuthority('MODERATOR_ROLE')")
    public String addNewLessonPage(){
        return "lessons/addNewLesson";
    }

    private static  String uploadDirectory =  "C:\\Users\\Acer\\IdeaProjects\\3.2\\springSecurity\\src\\main\\resources\\static\\img\\";

    @PostMapping(path = "addLesson")
    public String addLesson(Model model, @RequestParam(name = "title") String title, @RequestParam(name = "description") String description,
                            @RequestParam(name = "content") String content, @RequestParam(name = "img", required = false) MultipartFile multipartFile){

        if(!title.isEmpty() && !content.isEmpty() && !description.isEmpty()){
            try {

                //Get the file and save it somewhere
                byte[]bytes = multipartFile.getBytes();
                String fileName = multipartFile.getOriginalFilename();
                if(multipartFile.isEmpty()){
                    fileName = "slide-1.jpg";
                }
                Path path = Paths.get(uploadDirectory + fileName);
                Files.write(path, bytes);
                System.out.println("img = " + fileName);

                Lessons newLesson = new Lessons(null, title, description, content, getUserData(), new Date(), fileName, null);
                lessonsRepositories.save(newLesson);
                return "redirect:/lessons/lessons";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "redirect:/lessons/lessons?error";
    }

    @GetMapping(path = "/lessonDetails/{id}")
    public String lessonDetailsPage(Model model, @PathVariable(name = "id") Long id){

        Lessons lesson = lessonsRepositories.findByIdAndDeletedAtNull(id).get();
        if(!commentsRepositories.findAllByLesson(lesson).isEmpty()){
            model.addAttribute("comments", commentsRepositories.findAllByLessonOrderByPostDateDesc(lesson));
        }
        model.addAttribute("lesson", lessonsRepositories.findByIdAndDeletedAtNull(id).get());
        Users user = getUserData();
        if(user != null && (user.getRoles().equals("ADMIN_ROLE") || lesson.getAuthor().equals(user))) {
            model.addAttribute("user", user);
        }
        if(user != null && user==getUserData()){
            model.addAttribute("userCheck", user);
        }
        return "lessons/lessonDetails";
    }

    @GetMapping(path = "/editLesson/{id}")
    public String editPage(Model model, @PathVariable(name = "id") Long id){
        model.addAttribute("lesson", lessonsRepositories.findByIdAndDeletedAtNull(id).get());
        return "lessons/editLesson";
    }

    @PostMapping(path = "editLesson")
    public String editNews(Model model, @RequestParam(name = "id") Long id, @RequestParam(name = "title") String title,
                           @RequestParam(name = "description") String description, @RequestParam(name = "img", required = false) MultipartFile multipartFile,
                           @RequestParam(name = "content") String content){
        if(!title.isEmpty() && !content.isEmpty() && !description.isEmpty()){
            try {
                Optional<Lessons> lessons = lessonsRepositories.findById(id);
                //Get the file and save it somewhere
                byte[]bytes = multipartFile.getBytes();
                String fileName = multipartFile.getOriginalFilename();
                if(multipartFile.isEmpty()){
                    fileName = lessons.get().getFile();
                }
                Path path = Paths.get(uploadDirectory + fileName);
                Files.write(path, bytes);
                System.out.println("img = " + fileName);

                Lessons lesson = lessons.get();
                lesson.setTitle(title);
                lesson.setDescription(description);
                lesson.setContent(content);
                lesson.setFile(fileName);

                lessonsRepositories.save(lesson);
                return "redirect:/lessons/lessonDetails/"+id;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "redirect:/editLesson/"+id;
    }



    @PostMapping(path = "/deleteLesson")
    public String deletePost(@RequestParam(name = "id") Long id){
        Optional<Lessons> lesson = lessonsRepositories.findByIdAndDeletedAtNull(id);
        List<Comments> comments = commentsRepositories.findAllByLesson(lesson.get());
        commentsRepositories.deleteAll(comments);
        lessonsRepositories.delete(lesson.get());
        return "redirect:/lessons/lessons";
    }

    @PostMapping(path = "/addCommentLesson")
    public String AddComment(Model model, @RequestParam(name = "comment") String text, @RequestParam(name = "id") Long id){
        if(!text.isEmpty()){
            Comments comment = new Comments(null, getUserData(), null, lessonsRepositories.getOne(id), text, new Date());
            commentsRepositories.save(comment);
        }
        return "redirect:/lessons/lessonDetails/"+id;
    }

    @PostMapping(path = "/deleteCommentLesson")
    public String deleteCommentNews(@RequestParam(name = "id") Long id){
        Optional<Comments> comment = commentsRepositories.findById(id);
        Long idLesson = comment.get().getLesson().getId();

        commentsRepositories.delete(comment.get());
        return "redirect:/lessons/lessonDetails/"+ idLesson;
    }

}
