package com.sport.checkin.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlanConflictCheckResultDTO {

    private List<PlanConflictDTO> conflicts;

    private List<String> warnings;

    public PlanConflictCheckResultDTO() {
        this.conflicts = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public boolean hasConflicts() {
        return conflicts != null && !conflicts.isEmpty();
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    public void addConflict(PlanConflictDTO conflict) {
        if (this.conflicts == null) {
            this.conflicts = new ArrayList<>();
        }
        this.conflicts.add(conflict);
    }

    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }

    public void addAllConflicts(List<PlanConflictDTO> conflicts) {
        if (this.conflicts == null) {
            this.conflicts = new ArrayList<>();
        }
        if (conflicts != null) {
            this.conflicts.addAll(conflicts);
        }
    }

    public void addAllWarnings(List<String> warnings) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        if (warnings != null) {
            this.warnings.addAll(warnings);
        }
    }

}
