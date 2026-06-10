package com.sport.checkin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sport_type")
public class SportType {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String icon;

    private String description;

    private java.math.BigDecimal caloriePerMinute;

    private Integer sort;

    private Integer hotCount;

    private LocalDateTime lastHotUpdateTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

}
