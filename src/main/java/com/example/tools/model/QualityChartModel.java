package com.example.tools.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class QualityChartModel {
    private String time;

    private String node;

    private String host;

    private BigDecimal fps;
}
