package com.pp.economia_circular.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class OferenteValoracionResponseDto {
    private Long id;
    private Long reviewerId;
    private String reviewerName;
    private String reviewerEmail;
    private Long oferenteId;
    private String oferenteName;
    private Long articleId;
    private String articleTitle;
    private Integer rating;
    private String comment;
    private Boolean approved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
