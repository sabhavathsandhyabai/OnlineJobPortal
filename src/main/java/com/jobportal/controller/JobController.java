package com.jobportal.controller;

import com.jobportal.entity.Job;
import com.jobportal.service.JobService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping("/jobs")
    public String jobs(Model model) {

        model.addAttribute(
                "jobs",
                jobService.getAllJobs()
        );

        return "jobs";
    }

    @GetMapping("/add-job")
    public String addJobPage(Model model) {

        model.addAttribute("job", new Job());

        return "add-job";
    }

    @PostMapping("/add-job")
    public String addJob(@ModelAttribute Job job) {

        jobService.saveJob(job);

        return "redirect:/jobs";
    }

    @GetMapping("/delete-job/{id}")
    public String deleteJob(@PathVariable Long id) {

        jobService.deleteJob(id);

        return "redirect:/jobs";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam String keyword,
            Model model) {

        model.addAttribute(
                "jobs",
                jobService.searchJobs(keyword)
        );

        return "jobs";
    }
}
