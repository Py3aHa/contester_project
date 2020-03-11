package com.project.SptingSecurity.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @GetMapping(path = "/")
    public String indexPage(){
        return "index";
    }

    @GetMapping(path = "loginPage")
    public String loginPage(){

        return "login";
    }
}
