package com.example.unit_testing_automation;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class UnitTestingAutomationApplication {
	private static final String UPLOAD_DIR = "uploads/";


	public static void main(String[] args) {
		SpringApplication.run(UnitTestingAutomationApplication.class, args);
	}

	@Bean
	public CommandLineRunner createUploadFolder() {
		return args -> {
			Path path = Paths.get(UPLOAD_DIR);
			if (!Files.exists(path)) {
				try {
					Files.createDirectories(path);
					System.out.println("Upload directory created successfully.");
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Failed to create upload directory.");
				}

			}

		};
	}
}