package com.sport.checkin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanExecutionSnapshotDTO {

    private Long planId;

    private String title;

    private Long sportTypeId;

    private String sportTypeName;

    private Integer targetFrequency;

    private Integer targetDuration;

    private Integer completedCount;

    private Integer completedDuration;

    private BigDecimal completedCalorie;

    private Double completionRate;

    private String status;

    private String cycleStart;

    private String cycleEnd;

    private Integer daysElapsed;

    private Integer daysRemaining;

    private Double expectedProgress;

    private Double deviation;
}
