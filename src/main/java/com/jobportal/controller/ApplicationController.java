package com.jobportal.controller;

import com.jobportal.entity.Job;
import com.jobportal.entity.JobApplication;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.JobService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ApplicationController {

    private final JobService jobService;
    private final JobApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public ApplicationController(
            JobService jobService,
            JobApplicationRepository applicationRepository,
            UserRepository userRepository) {

        this.jobService = jobService;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    // Show application page
    @GetMapping("/apply/{id}")
    public String applyPage(
            @PathVariable Long id,
            Model model) {

        Job job = jobService.getJobById(id);

        if (job == null) {
            return "redirect:/jobs";
        }

        model.addAttribute("job", job);

        return "apply";
    }

    // Submit application
    @PostMapping("/apply")
    public String apply(
            @RequestParam Long jobId,
            @RequestParam String jobTitle,
            Authentication authentication) {

        String email = authentication.getName();

        var user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        JobApplication application = new JobApplication(
                user.getName(),
                user.getEmail(),
                jobId,
                jobTitle,
                "Applied"
        );

        applicationRepository.save(application);

        return "redirect:/my-applications";
    }

    // Show logged-in user's applications
    @GetMapping("/my-applications")
    public String myApplications(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        model.addAttribute(
                "applications",
                applicationRepository.findByApplicantEmail(email)
        );

        return "my-applications";
    }
}