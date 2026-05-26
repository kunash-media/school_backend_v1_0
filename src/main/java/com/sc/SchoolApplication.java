package com.sc;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class SchoolApplication {

	public static void main(String[] args) {

		// Load .env file (if present)
		Dotenv dotenv = Dotenv.configure()
				.directory("./") // Look in root folder
				.ignoreIfMissing() // Avoid errors if .env is missing (e.g., in production)
				.load();

		// Set system properties from .env
		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue())
		);

		// Set the default JVM timezone to India Standard Time (IST)
		// This ensures LocalDateTime.now(), logging
		// timestamps, etc., use IST
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));

		// Optional: Log to confirm
		System.out.println("Default TimeZone set to: " + TimeZone.getDefault().getID());
        SpringApplication.run(SchoolApplication.class, args);
	}
}