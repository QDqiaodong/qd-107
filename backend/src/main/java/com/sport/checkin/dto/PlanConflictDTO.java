package com.sport.checkin.dto;

import lombok.Data;

@Data
public class PlanConflictDTO {

    private String conflictType;

    private String conflictDescription;

    private Long conflictingPlanId;

    private String conflictingPlanTitle;

    private String conflictDetail;

}
