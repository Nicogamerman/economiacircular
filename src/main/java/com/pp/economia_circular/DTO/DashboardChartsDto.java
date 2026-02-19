package com.pp.economia_circular.DTO;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class DashboardChartsDto {

    private Map<String, Long> usuariosPorMes;
    private Map<String, Long> articulosPorCategoria;
    private Map<String, Long> intercambiosPorMes;
}
