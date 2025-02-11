CREATE DATABASE net_analysis;
USE net_analysis;
CREATE TABLE IF NOT EXISTS measurement_info
(
    `type` TINYINT NOT NULL COMMENT "类型，0-最小时间窗口, 1-分钟, 2-小时",
    `timestamp` LARGEINT NOT NULL COMMENT "秒级时间戳",
    `switch_id` CHAR(32) NOT NULL COMMENT "交换机id（UUID）",
    `date` DATE NOT NULL COMMENT "数据生成日期",
    `heavy_change_keys` JSON NOT NULL COMMENT "heavy_change 的键",
    `heavy_hitter_keys` JSON NOT NULL COMMENT "heavy_hitter 的键",
    `depth_for_size_cm` INT NOT NULL COMMENT "流大小估计 cm sketch 的深度",
    `width_for_size_cm` INT NOT NULL COMMENT "流大小估计 cm sketch 的宽度",
    `size_cm` ARRAY<ARRAY<INT>> NOT NULL COMMENT "流大小估计 cm sketch",
    `depth_for_count_cm` INT NOT NULL COMMENT "频数估计 cm sketch 的深度",
    `width_for_count_cm` INT NOT NULL COMMENT "频数估计 cm sketch 的宽度",
    `count_cm` ARRAY<ARRAY<INT>> NOT NULL COMMENT "频数估计 cm sketch"
)
    ENGINE=OLAP
    UNIQUE KEY(`type`, `timestamp`, `switch_id`)
PARTITION BY LIST(`type`)
(
    PARTITION `p_sec` VALUES IN (0),
    PARTITION `p_min` VALUES IN (1),
    PARTITION `p_hour` VALUES IN (2)
)
DISTRIBUTED BY HASH(`switch_id`) BUCKETS 16
PROPERTIES
(
    "replication_num" = "1"
);