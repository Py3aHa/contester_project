package com.project.SptingSecurity.controllers;

import com.project.SptingSecurity.Repositories.NewPostsRepositories;
import com.project.SptingSecurity.Repositories.RolesRepositories;
import com.project.SptingSecurity.Repositories.UserRepositories;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.jws.WebParam;
import java.io.IOException;
import java.nio.file.*;
import java.util.Date;

@Controller
public class NewsController {
    @Autowired
    UserService userService;

    @Autowired
    UserRepositories userRepositories;

    @Autowired
    RolesRepositories rolesRepositories;

    @Autowired
    NewPostsRepositories newPostsRepositories;

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
        return "addNewPost";
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
                return "index";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "redirect:/addNewPost?error";
    }

    @GetMapping(path = "/details/{id}")
    public String DetailsPage(Model model, @PathVariable(name = "id") Long id){
        model.addAttribute("news", newPostsRepositories.findByIdAndDeletedAtNull(id).get());
        model.addAttribute("user", getUserData());
        return "details";
    }
}
