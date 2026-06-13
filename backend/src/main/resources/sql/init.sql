CREATE DATABASE IF NOT EXISTS fitness_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fitness_db;

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    gender TINYINT DEFAULT 0 COMMENT '性别：0未知 1男 2女',
    age INT COMMENT '年龄',
    height DECIMAL(5,2) COMMENT '身高(cm)',
    weight DECIMAL(5,2) COMMENT '体重(kg)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP TABLE IF EXISTS sport_type;
CREATE TABLE sport_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '类型ID',
    name VARCHAR(50) NOT NULL COMMENT '运动名称',
    icon VARCHAR(255) COMMENT '图标',
    description VARCHAR(500) COMMENT '描述',
    calorie_per_minute DECIMAL(10,2) COMMENT '每分钟消耗卡路里',
    sort INT DEFAULT 0 COMMENT '排序',
    hot_count INT DEFAULT 0 COMMENT '热度计数',
    last_hot_update_time DATETIME COMMENT '最后热度更新时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0未删除 1已删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动类型表';

DROP TABLE IF EXISTS sport_plan;
CREATE TABLE sport_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '计划ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    sport_type_id BIGINT NOT NULL COMMENT '运动类型ID',
    title VARCHAR(100) NOT NULL COMMENT '计划标题',
    description VARCHAR(500) COMMENT '计划描述',
    target_duration INT COMMENT '目标时长(分钟)',
    target_frequency INT COMMENT '目标频率(次/周)',
    start_date DATE COMMENT '开始日期',
    end_date DATE COMMENT '结束日期',
    reminder_time TIME COMMENT '提醒时间',
    reminder_enabled TINYINT DEFAULT 0 COMMENT '是否开启提醒：0否 1是',
    weekdays VARCHAR(50) COMMENT '训练日（周几，逗号分隔：0周日1周一...6周六）',
    training_time_slot VARCHAR(20) COMMENT '主要训练时段：MORNING早间 FORENOON上午 AFTERNOON下午 EVENING晚间 NIGHT夜间',
    status TINYINT DEFAULT 1 COMMENT '状态：0停用 1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0未删除 1已删除',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动计划表';

DROP TABLE IF EXISTS checkin_record;
CREATE TABLE checkin_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '打卡ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    sport_type_id BIGINT NOT NULL COMMENT '运动类型ID',
    plan_id BIGINT COMMENT '关联计划ID',
    duration INT NOT NULL COMMENT '运动时长(分钟)',
    calorie DECIMAL(10,2) COMMENT '消耗卡路里',
    distance DECIMAL(10,2) COMMENT '距离(公里)',
    intensity VARCHAR(20) DEFAULT 'MODERATE' COMMENT '训练强度：LIGHT轻量 MODERATE中等 HIGH高强度',
    remark VARCHAR(500) COMMENT '打卡备注',
    muscle_tags VARCHAR(255) COMMENT '肌感标签，多个逗号分隔',
    images VARCHAR(1000) COMMENT '打卡图片，多个逗号分隔',
    checkin_date DATE NOT NULL COMMENT '打卡日期',
    checkin_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '打卡时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0未删除 1已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_checkin_date (checkin_date),
    INDEX idx_sport_type_id (sport_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录表';

DROP TABLE IF EXISTS sport_dynamic;
CREATE TABLE sport_dynamic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '动态ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    checkin_id BIGINT COMMENT '关联打卡ID',
    sport_type_id BIGINT NOT NULL COMMENT '运动类型ID',
    content VARCHAR(1000) NOT NULL COMMENT '动态内容',
    images VARCHAR(1000) COMMENT '动态图片，多个逗号分隔',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    is_public TINYINT DEFAULT 1 COMMENT '是否公开：0私密 1公开',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记：0未删除 1已删除',
    INDEX idx_user_id (user_id),
    INDEX idx_sport_type_id (sport_type_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动动态表';

DROP TABLE IF EXISTS dynamic_like;
CREATE TABLE dynamic_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞ID',
    dynamic_id BIGINT NOT NULL COMMENT '动态ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_dynamic_user (dynamic_id, user_id),
    INDEX idx_dynamic_id (dynamic_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='动态点赞表';

INSERT INTO sport_type (name, icon, description, calorie_per_minute, sort, hot_count) VALUES
('跑步', 'running', '户外或室内跑步，有氧运动', 10.00, 1, 1000),
('骑行', 'cycling', '户外骑行运动', 8.00, 2, 850),
('游泳', 'swimming', '游泳运动，全身锻炼', 12.00, 3, 720),
('瑜伽', 'yoga', '瑜伽拉伸，身心放松', 4.00, 4, 680),
('健身', 'fitness', '健身房力量训练', 7.00, 5, 900),
('羽毛球', 'badminton', '羽毛球运动', 6.50, 6, 560),
('篮球', 'basketball', '篮球运动', 9.00, 7, 780),
('徒步', 'hiking', '户外徒步登山', 5.50, 8, 450);
