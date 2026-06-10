package com.sport.checkin.dto;

import com.sport.checkin.entity.CheckinRecord;
import lombok.Data;

@Data
public class CheckinResultDTO {

    private CheckinRecord record;

    private Boolean merged;

    private String mergeTip;

    public static CheckinResultDTO of(CheckinRecord record, boolean merged, String mergeTip) {
        CheckinResultDTO dto = new CheckinResultDTO();
        dto.setRecord(record);
        dto.setMerged(merged);
        dto.setMergeTip(mergeTip);
        return dto;
    }
}
