package com.app.easypharma_backend.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for serving well-known configuration files used for Deep Linking
 * (Android App Links and iOS Universal Links).
 */
@RestController
@Slf4j
@Tag(name = "Deep Linking", description = "Endpoints for mobile app association (.well-known)")
public class DeepLinkingController {

    private final ResourceLoader resourceLoader;

    public DeepLinkingController(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Serves the Android Asset Links file.
     */
    @GetMapping(value = "/.well-known/assetlinks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Android Asset Links", description = "Serves the configuration for Android App Links")
    public ResponseEntity<Resource> getAssetLinks() {
        log.info("Request received for Android Asset Links (.well-known/assetlinks.json)");
        Resource resource = resourceLoader.getResource("classpath:.well-known/assetlinks.json");
        if (!resource.exists()) {
            log.error("assetlinks.json not found in classpath");
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
            .header("ngrok-skip-browser-warning", "true")
            .contentType(MediaType.APPLICATION_JSON)
            .body(resource);
    }

    /**
     * Serves the iOS Apple App Site Association file.
     */
    @GetMapping(value = "/.well-known/apple-app-site-association", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "iOS Apple App Site Association", description = "Serves the configuration for iOS Universal Links")
    public ResponseEntity<Resource> getAppleAppSiteAssociation() {
        log.info("Request received for iOS Universal Links (.well-known/apple-app-site-association)");
        Resource resource = resourceLoader.getResource("classpath:.well-known/apple-app-site-association");
        if (!resource.exists()) {
            log.error("apple-app-site-association not found in classpath");
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(resource);
    }
}
