package com.pp.economia_circular.DTO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class OferenteValoracionesSummaryDto {
    private Long oferenteId;
    private Double averageRating;
    private Long totalReviews;
    private List<OferenteValoracionResponseDto> reviews;
}
