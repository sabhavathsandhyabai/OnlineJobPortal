package com.jobportal.controller;

import com.jobportal.entity.User;
import com.jobportal.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Registration page
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // Register new user
    @PostMapping("/register")
    public String register(@ModelAttribute User user) {

        userService.register(user);

        return "redirect:/login";
    }

    // Login page
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
}