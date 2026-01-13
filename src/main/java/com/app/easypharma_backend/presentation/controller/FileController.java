package com.app.easypharma_backend.presentation.controller;

import com.app.easypharma_backend.application.common.dto.ApiResponse;
import com.app.easypharma_backend.infrastructure.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Gestion des Fichiers", description = "Upload et téléchargement de fichiers")
public class FileController {

    private final FileStorageService fileStorageService;

    /**
     * Upload un fichier
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload un fichier", description = "Upload une image ou un PDF (max 5MB)")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @Parameter(description = "Fichier à uploader", required = true) @RequestParam("file") MultipartFile file,
            @Parameter(description = "Sous-répertoire de stockage (ex: documents, prescriptions)") @RequestParam(value = "subdirectory", defaultValue = "general") String subdirectory) {

        log.info("Upload request for file: {} in subdirectory: {}", file.getOriginalFilename(), subdirectory);

        String filePath = fileStorageService.storeFile(file, subdirectory);

        Map<String, String> response = new HashMap<>();
        response.put("fileUrl", "/api/v1/files/" + filePath);
        response.put("fileName", file.getOriginalFilename());

        return ResponseEntity.ok(ApiResponse.success(response, "Fichier uploadé avec succès"));
    }

    /**
     * Télécharge un fichier
     */
    @GetMapping("/{subdirectory}/{filename:.+}")
    @Operation(summary = "Télécharge un fichier", description = "Récupère un fichier uploadé")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "Sous-répertoire", required = true) @PathVariable String subdirectory,
            @Parameter(description = "Nom du fichier", required = true) @PathVariable String filename) {

        log.info("Download request for file: {}/{}", subdirectory, filename);

        Resource resource = fileStorageService.loadFileAsResource(subdirectory, filename);

        // Déterminer le type de contenu
        String contentType = "application/octet-stream";
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            contentType = "image/jpeg";
        } else if (filename.endsWith(".png")) {
            contentType = "image/png";
        } else if (filename.endsWith(".gif")) {
            contentType = "image/gif";
        } else if (filename.endsWith(".pdf")) {
            contentType = "application/pdf";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    /**
     * Supprime un fichier
     */
    @DeleteMapping("/{subdirectory}/{filename:.+}")
    @Operation(summary = "Supprime un fichier", description = "Supprime un fichier uploadé")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "Sous-répertoire", required = true) @PathVariable String subdirectory,
            @Parameter(description = "Nom du fichier", required = true) @PathVariable String filename) {

        log.info("Delete request for file: {}/{}", subdirectory, filename);

        fileStorageService.deleteFile(subdirectory, filename);

        return ResponseEntity.ok(ApiResponse.success(null, "Fichier supprimé avec succès"));
    }
}
