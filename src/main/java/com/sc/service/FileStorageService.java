package com.sc.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:./uploads/notices}")
    private String uploadDir;

    public String storeFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String original  = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        int    dotIndex  = original.lastIndexOf('.');
        String extension = dotIndex > 0 ? original.substring(dotIndex).toLowerCase() : "";

        if (!extension.matches("\\.(pdf|jpg|jpeg|png)"))
            throw new IllegalArgumentException("Only PDF, JPG, PNG allowed. Got: " + extension);
        if (file.getSize() > 5L * 1024 * 1024)
            throw new IllegalArgumentException("File size must not exceed 5MB");

        String storedName = UUID.randomUUID() + extension;
        Files.copy(file.getInputStream(), uploadPath.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
        return storedName;
    }

    public Resource loadFileAsResource(String storedName) throws MalformedURLException {
        Path     path     = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(storedName);
        Resource resource = new UrlResource(path.toUri());
        if (!resource.exists()) throw new RuntimeException("File not found: " + storedName);
        return resource;
    }

    public void deleteFile(String storedName) {
        try {
            Files.deleteIfExists(Paths.get(uploadDir).toAbsolutePath().normalize().resolve(storedName));
        } catch (IOException e) {
            System.err.println("[FileStorage] Could not delete: " + storedName);
        }
    }

    public String getContentType(String storedName) {
        String lower = storedName.toLowerCase();
        if (lower.endsWith(".pdf"))               return "application/pdf";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png"))               return "image/png";
        return "application/octet-stream";
    }
}