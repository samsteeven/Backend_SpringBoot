package com.app.easypharma_backend.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "application/pdf");
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".pdf");

    public FileStorageService(@Value("${app.file-storage.upload-dir:./uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.",
                    ex);
        }
    }

    /**
     * Valide le type MIME du fichier
     */
    private void validateMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new FileStorageException("Type de fichier non autorisé. Types acceptés: JPG, PNG, GIF, PDF");
        }
    }

    /**
     * Valide la taille du fichier
     */
    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException("Fichier trop volumineux. Taille maximale: 5MB");
        }
    }

    /**
     * Valide l'extension du fichier
     */
    private void validateExtension(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileStorageException(
                    "Extension de fichier non autorisée. Extensions acceptées: " + ALLOWED_EXTENSIONS);
        }
    }

    public String storeFile(MultipartFile file, String subdirectory) {
        // Validations
        validateFileSize(file);
        validateMimeType(file);

        // Normalize file name
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        // Check if the file's name contains invalid characters
        if (originalFileName.contains("..")) {
            throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFileName);
        }

        // Validate extension
        validateExtension(originalFileName);

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

    /**
     * Charge un fichier en tant que Resource
     */
    public Resource loadFileAsResource(String subdirectory, String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(subdirectory).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("File not found: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found: " + filename, ex);
        }
    }

    /**
     * Supprime un fichier
     */
    public void deleteFile(String subdirectory, String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(subdirectory).resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file: " + filename, ex);
        }
    }
}
