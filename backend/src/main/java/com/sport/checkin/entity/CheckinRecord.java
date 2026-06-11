package com.sport.checkin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("checkin_record")
public class CheckinRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long sportTypeId;

    private Long planId;

    private Integer duration;

    private java.math.BigDecimal calorie;

    private java.math.BigDecimal distance;

    private String intensity;

    private String remark;

    private String muscleTags;

    private String images;

    private LocalDate checkinDate;

    private LocalDateTime checkinTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

}
