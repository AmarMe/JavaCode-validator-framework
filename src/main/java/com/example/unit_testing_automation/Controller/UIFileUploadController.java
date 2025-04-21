package com.example.unit_testing_automation.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3000")
public class UIFileUploadController {
    private static final String UPLOAD_DIR = "uploads/";




    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        String originalFileName = file.getOriginalFilename();

        // Validate file type (.java file)
        if (file.isEmpty() || originalFileName == null || !originalFileName.endsWith(".java")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"message\":\"Please upload a valid .java file.\"}");
        }

        try {
            // Create directories if not exist
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            // Write the file to the server (without changing the file name)
            Path filePath = Paths.get(UPLOAD_DIR + originalFileName);
            Files.write(filePath, file.getBytes());

            // Return success response
            return ResponseEntity.ok().body("{\"message\":\"" + originalFileName + " successfully uploaded!\"}");

        } catch (IOException e) {
            e.printStackTrace();
            // Handle errors with file upload
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"message\":\"Failed to upload file.\"}");
        }
    }

}


