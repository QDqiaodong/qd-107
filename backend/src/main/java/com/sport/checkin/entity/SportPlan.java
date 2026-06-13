package com.sport.checkin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("sport_plan")
public class SportPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long sportTypeId;

    private String title;

    private String description;

    private Integer targetDuration;

    private Integer targetFrequency;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalTime reminderTime;

    private Integer reminderEnabled;

    private String weekdays;

    private String trainingTimeSlot;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

}
