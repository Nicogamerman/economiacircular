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
    private Long articleId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
