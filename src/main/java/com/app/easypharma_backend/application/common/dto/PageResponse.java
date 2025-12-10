package com.app.easypharma_backend.application.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    @Schema(description = "Contenu de la page")
    private List<T> content;
    
    @Schema(description = "Numéro de la page (commence à 0)")
    private int pageNumber;
    
    @Schema(description = "Taille de la page")
    private int pageSize;
    
    @Schema(description = "Nombre total d'éléments")
    private long totalElements;
    
    @Schema(description = "Nombre total de pages")
    private int totalPages;
    
    @Schema(description = "Indique s'il y a une page suivante")
    private boolean hasNext;
    
    @Schema(description = "Indique s'il y a une page précédente")
    private boolean hasPrevious;

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}