package com.app.easypharma_backend.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${app.file-storage.upload-dir:./uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.",
                    ex);
        }
    }

    public String storeFile(MultipartFile file, String subdirectory) {
        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        // Check if the file's name contains invalid characters
        if (originalFileName.contains("..")) {
            throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFileName);
        }

        // Generate a unique filename to prevent collisions
        String fileExtension = "";
        try {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        } catch (Exception e) {
            fileExtension = "";
        }

        String newFileName = UUID.randomUUID().toString() + fileExtension;

        try {
            // Target directory
            Path targetDir = this.fileStorageLocation.resolve(subdirectory);
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            // Copy file to the target location (replacing existing file with the same name)
            Path targetLocation = targetDir.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path or URL
            return subdirectory + "/" + newFileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }
}
