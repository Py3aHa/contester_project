package com.project.SptingSecurity.controllers;

import com.project.SptingSecurity.Repositories.NewPostsRepositories;
import com.project.SptingSecurity.Repositories.RolesRepositories;
import com.project.SptingSecurity.Repositories.UserRepositories;
import com.project.SptingSecurity.entities.NewPosts;
import com.project.SptingSecurity.entities.Roles;
import com.project.SptingSecurity.entities.Users;
import com.project.SptingSecurity.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;
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
import sun.security.util.Length;

import javax.jws.WebParam;
import javax.servlet.annotation.ServletSecurity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

//https://habr.com/ru/post/350870/
//https://habr.com/ru/post/437658/
//https://habr.com/ru/post/435080/
//https://habr.com/ru/post/435062/
//https://habr.com/ru/post/435080/
//https://habr.com/ru/post/350870/
//https://habr.com/ru/post/351304/
//https://www.codeflow.site/ru/article/thymeleaf-iteration
//https://www.codeflow.site/ru/article/spring-thymeleaf-3-expressions
//https://habr.com/ru/post/352556/
//https://github.com/thymeleaf/thymeleaf-extras-springsecurity

@Controller
public class MainController {

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

    @GetMapping(path = "/")
    public String indexPage(Model model, @RequestParam(name = "page", defaultValue = "1") int page){
        boolean userOrNot = false;

        int size = newPostsRepositories.countAllByDeletedAtNull();

        int tabSize = (size+4)/10;

        if(tabSize < 1){
            tabSize = 1;
        }

        Pageable pageable = PageRequest.of(page-1,5);
        List<NewPosts> posts = newPostsRepositories.findAllByDeletedAtNullOrderByPostDateDesc(pageable);
        model.addAttribute("tabSize", tabSize);
        model.addAttribute("news", posts);
        model.addAttribute("user", getUserData());
        return "index";
    }
                                                //Registration
    @GetMapping(path = "registrationPage")
    public String registrationPage(Model model, @RequestParam(name = "error", required = false) String error){
        String [] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        model.addAttribute("months", months);
        System.out.println("error = " + error);
        model.addAttribute("error", error);
        return "registration";
    }

    @PostMapping(path = "registration")
    public String registration(@RequestParam(name = "email") String email, @RequestParam(name = "name", required = false, defaultValue = "") String name, @RequestParam(name = "password") String password,
                               @RequestParam(name = "rePass") String rePass, @RequestParam(name = "day") int day, @RequestParam(name = "month") String month,
                               @RequestParam(name = "year") int year, Model model){
        Users user = userRepositories.findByEmail(email);
        String redirect = "redirect:/registrationPage?error";
        if(user == null){
            if(email.indexOf('@') > 3) {
                if (password.equals(rePass)) {
                    if (password.length() == 6) {
                        String date = day + "." + month + "." + year;
                        Roles role = rolesRepositories.getOne(2L);
                        Set<Roles> roles = new HashSet<>();
                        roles.add(role);
                        user = new Users(null, email, password, name, date, true, roles);
                        userService.registerUser(user);

                        redirect = "redirect:/login?success";
                    }
                }
            }
        }
        return redirect;
    }

                                                //Login
    @GetMapping(path = "login")
    public String loginPage(Model model, @RequestParam(name = "error", required = false) String error){
        return "login";
    }
}
