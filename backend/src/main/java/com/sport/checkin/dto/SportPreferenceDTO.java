package com.sport.checkin.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class SportPreferenceDTO {

    private Long userId;

    private Integer totalCheckinCount;

    private BigDecimal averageDuration;

    private List<SportItem> frequentSports;

    private List<TimeSlot> preferredTimeSlots;

    private List<WeekdayFreq> frequentWeekdays;

    private String summary;

    @Data
    public static class SportItem {
        private Long sportTypeId;
        private String sportTypeName;
        private String icon;
        private Integer count;
        private Integer totalDuration;
        private BigDecimal percentage;
    }

    @Data
    public static class TimeSlot {
        private String slotName;
        private String slotRange;
        private Integer count;
        private BigDecimal percentage;
    }

    @Data
    public static class WeekdayFreq {
        private Integer weekday;
        private String weekdayName;
        private Integer count;
        private BigDecimal percentage;
    }
}
