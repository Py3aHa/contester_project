package com.project.SptingSecurity.controllers;

import com.project.SptingSecurity.Repositories.*;
import com.project.SptingSecurity.entities.Comments;
import com.project.SptingSecurity.entities.NewPosts;
import com.project.SptingSecurity.entities.Users;
import com.project.SptingSecurity.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.nio.file.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(path = "/news")
public class NewsController {
    @Autowired
    UserService userService;

    @Autowired
    UserRepositories userRepositories;

    @Autowired
    RolesRepositories rolesRepositories;

    @Autowired
    NewPostsRepositories newPostsRepositories;

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

    private static  String uploadDirectory =  "C:\\Users\\Acer\\IdeaProjects\\3.2\\springSecurity\\src\\main\\resources\\static\\img\\";

    @GetMapping(path = "/addNewPost")
    @PreAuthorize("hasAuthority('MODERATOR_ROLE')")
    public String addNewPost(){
        return "news/addNewPost";
    }

    @PostMapping(path = "/addPost")
    public String addPost(Model model, @RequestParam(name = "img", required = false) MultipartFile multipartFile, @RequestParam(name = "title") String title,
                          @RequestParam(name = "content") String content, @RequestParam(name = "description") String description){

        if(!title.isEmpty() && !content.isEmpty() && !description.isEmpty()){
            try {

                //Get the file and save it somewhere
                byte[]bytes = multipartFile.getBytes();
                String fileName = multipartFile.getOriginalFilename();
                if(multipartFile.isEmpty()){
                    fileName = "news.jpg";
                }
                Path path = Paths.get(uploadDirectory + fileName);
                Files.write(path, bytes);
                System.out.println("img = " + fileName);

                NewPosts newPost = new NewPosts(null, title, description, content, getUserData(), new Date(), fileName, null);
                newPostsRepositories.save(newPost);
                return "redirect:/";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "redirect:/news/addNewPost?error";
    }

    @GetMapping(path = "/details/{id}")
    public String DetailsPage(Model model, @PathVariable(name = "id") Long id){
        NewPosts newPost = newPostsRepositories.findByIdAndDeletedAtNull(id).get();
        if(!commentsRepositories.findAllByNewsPost(newPost).isEmpty()){
            model.addAttribute("comments", commentsRepositories.findAllByNewsPostOrderByPostDateDesc(newPost));
        }
        model.addAttribute("news", newPostsRepositories.findByIdAndDeletedAtNull(id).get());
        Users user = getUserData();
        if(user != null && (user.getRoles().equals("ADMIN_ROLE") || newPost.getAuthor().equals(user))) {
            model.addAttribute("user", user);
        }
        if(user != null && user==getUserData()){
            model.addAttribute("userCheck", user);
        }
        return "news/details";
    }

    @PostMapping(path = "/addComment")
    public String AddComment(Model model, @RequestParam(name = "comment") String text, @RequestParam(name = "id") Long id){
        if(!text.isEmpty()){
            Comments comment = new Comments(null, getUserData(), newPostsRepositories.getOne(id), null, text, new Date());
            commentsRepositories.save(comment);
        }
        return "redirect:/news/details/"+id;
    }

    @GetMapping(path = "/editNews/{id}")
    public String editPage(Model model, @PathVariable(name = "id") Long id){
        model.addAttribute("post", newPostsRepositories.findByIdAndDeletedAtNull(id).get());
        return "news/editNews";
    }

    @PostMapping(path = "editNews")
    public String editNews(Model model, @RequestParam(name = "id") Long id, @RequestParam(name = "title") String title,
                           @RequestParam(name = "description") String description, @RequestParam(name = "img", required = false) MultipartFile multipartFile,
                           @RequestParam(name = "content") String content){
        if(!title.isEmpty() && !content.isEmpty() && !description.isEmpty()){
            try {
                Optional<NewPosts> newPosts = newPostsRepositories.findById(id);
                //Get the file and save it somewhere
                byte[]bytes = multipartFile.getBytes();
                String fileName = multipartFile.getOriginalFilename();
                if(multipartFile.isEmpty()){
                    fileName = newPosts.get().getFile();
                }
                Path path = Paths.get(uploadDirectory + fileName);
                Files.write(path, bytes);
                System.out.println("img = " + fileName);

                NewPosts post = newPosts.get();
                post.setTitle(title);
                post.setDescription(description);
                post.setContent(content);
                post.setFile(fileName);

                newPostsRepositories.save(post);
                return "redirect:/news/details/"+id;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "redirect:/news/editNews/"+id;
    }

    @PostMapping(path = "/deletePost")
    public String deletePost(@RequestParam(name = "id") Long id){
        Optional<NewPosts> post = newPostsRepositories.findByIdAndDeletedAtNull(id);
        List<Comments> comments = commentsRepositories.findAllByNewsPost(post.get());
        commentsRepositories.deleteAll(comments);
        newPostsRepositories.delete(post.get());
        return "redirect:/";
    }

    @PostMapping(path = "/deleteCommentNews")
    public String deleteCommentNews(@RequestParam(name = "id") Long id){
        Optional<Comments> comment = commentsRepositories.findById(id);
        Long idPost = comment.get().getNewsPost().getId();
        commentsRepositories.delete(comment.get());
        return "redirect:/news/details/"+ idPost;
    }


}