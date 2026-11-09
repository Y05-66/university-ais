-- 为 course_offer 表添加乐观锁版本号字段，用于并发选课控制
ALTER TABLE IF EXISTS course_offer ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号';
