package com.jobportal.controller;

import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    // Folder where uploaded files will be stored
    private final Path uploadDir = Paths.get("uploads");

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;

        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder", e);
        }
    }


    // =========================
    // SHOW PROFILE
    // =========================
    @GetMapping("/profile")
    public String profile(
            Authentication authentication,
            Model model) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        model.addAttribute("user", user);

        return "profile";
    }


    // =========================
    // UPDATE PROFILE DETAILS
    // =========================
    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute User user,
            Authentication authentication) {

        String email = authentication.getName();

        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        existingUser.setName(user.getName());
        existingUser.setPhone(user.getPhone());
        existingUser.setLocation(user.getLocation());
        existingUser.setSkills(user.getSkills());
        existingUser.setEducation(user.getEducation());
        existingUser.setExperience(user.getExperience());

        userRepository.save(existingUser);

        return "redirect:/profile?success=true";
    }


    // =========================
    // UPLOAD PROFILE IMAGE + RESUME
    // =========================
    @PostMapping("/profile/upload")
    public String uploadFiles(
            @RequestParam(value = "profileImage", required = false)
            MultipartFile profileImage,

            @RequestParam(value = "resume", required = false)
            MultipartFile resume,

            Authentication authentication) throws IOException {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));


        // -------------------------
        // PROFILE IMAGE
        // -------------------------
        if (profileImage != null && !profileImage.isEmpty()) {

            String originalName =
                    profileImage.getOriginalFilename();

            String extension = "";

            if (originalName != null &&
                    originalName.contains(".")) {

                extension =
                        originalName.substring(
                                originalName.lastIndexOf("."));
            }

            String fileName =
                    "profile_" +
                            UUID.randomUUID() +
                            extension;

            Path filePath =
                    uploadDir.resolve(fileName);

            Files.copy(
                    profileImage.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            user.setProfileImage(fileName);
        }


        // -------------------------
        // RESUME
        // -------------------------
        if (resume != null && !resume.isEmpty()) {

            String originalName =
                    resume.getOriginalFilename();

            String extension = "";

            if (originalName != null &&
                    originalName.contains(".")) {

                extension =
                        originalName.substring(
                                originalName.lastIndexOf("."));
            }

            String fileName =
                    "resume_" +
                            UUID.randomUUID() +
                            extension;

            Path filePath =
                    uploadDir.resolve(fileName);

            Files.copy(
                    resume.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            user.setResume(fileName);
        }


        // Save filenames into MySQL
        userRepository.save(user);

        return "redirect:/profile?uploadSuccess=true";
    }


    // =========================
    // SHOW PROFILE IMAGE
    // =========================
    @GetMapping("/profile/image")
    public ResponseEntity<Resource> getProfileImage(
            Authentication authentication)
            throws MalformedURLException {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getProfileImage() == null ||
                user.getProfileImage().isEmpty()) {

            return ResponseEntity.notFound().build();
        }

        Path filePath =
                uploadDir.resolve(user.getProfileImage());

        Resource resource =
                new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }


    // =========================
    // VIEW RESUME
    // =========================
    @GetMapping("/profile/resume")
    public ResponseEntity<Resource> viewResume(
            Authentication authentication)
            throws MalformedURLException {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getResume() == null ||
                user.getResume().isEmpty()) {

            return ResponseEntity.notFound().build();
        }

        Path filePath =
                uploadDir.resolve(user.getResume());

        Resource resource =
                new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }


    // =========================
    // DOWNLOAD RESUME
    // =========================
    @GetMapping("/profile/resume/download")
    public ResponseEntity<Resource> downloadResume(
            Authentication authentication)
            throws MalformedURLException {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getResume() == null ||
                user.getResume().isEmpty()) {

            return ResponseEntity.notFound().build();
        }

        Path filePath =
                uploadDir.resolve(user.getResume());

        Resource resource =
                new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                user.getResume() + "\""
                )
                .body(resource);
    }
}