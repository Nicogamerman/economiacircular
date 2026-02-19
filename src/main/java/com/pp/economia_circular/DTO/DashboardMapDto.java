package com.pp.economia_circular.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DashboardMapDto {

    private List<MapPointDto> recyclingCenters;
    private List<MapPointDto> events;
}
