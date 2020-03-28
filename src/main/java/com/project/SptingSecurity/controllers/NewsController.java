package com.project.SptingSecurity.controllers;

import com.project.SptingSecurity.Repositories.NewPostsRepositories;
import com.project.SptingSecurity.Repositories.RolesRepositories;
import com.project.SptingSecurity.Repositories.UserRepositories;
import com.project.SptingSecurity.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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

    @GetMapping(path = "/addNewPost")

    public String addNewPost(){
        return "addNewPost";
    }
}
