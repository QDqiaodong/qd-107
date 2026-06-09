package com.sport.checkin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sport_dynamic")
public class SportDynamic {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long checkinId;

    private Long sportTypeId;

    private String content;

    private String images;

    private Integer likeCount;

    private Integer commentCount;

    private Integer viewCount;

    private Integer isPublic;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

}
