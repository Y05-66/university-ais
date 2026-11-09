-- =====================================================
-- 优化1: 密码迁移 SHA-256 → BCrypt
-- 优化3: 补充分布式索引
-- =====================================================

-- 1. BCrypt 哈希 (固定 salt 用于开发环境迁移)
UPDATE sys_user SET password = '$2a$10$YCVK4E3uMXIVfTJSmMPNTOLSVRyJWgFtXqU3V4lPp9zBqL6nO0yG.' WHERE username IN ('admin', 'teacher01', 'student01', 'jiaowu');

-- 2. 补充分布式索引 (使用存储过程跳过已存在的索引)
DROP PROCEDURE IF EXISTS add_index_if_not_exists;
DELIMITER //
CREATE PROCEDURE add_index_if_not_exists(IN tbl VARCHAR(64), IN idx VARCHAR(64), IN cols VARCHAR(255))
BEGIN
  IF NOT EXISTS (SELECT 1 FROM information_schema.statistics
                 WHERE table_schema=DATABASE() AND table_name=tbl AND index_name=idx) THEN
    SET @sql = CONCAT('ALTER TABLE ', tbl, ' ADD INDEX ', idx, ' (', cols, ')');
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END //
DELIMITER ;
CALL add_index_if_not_exists('sys_user_role', 'idx_role_id', 'role_id');
CALL add_index_if_not_exists('sys_log', 'idx_log_user_time', 'user_id, create_time');
CALL add_index_if_not_exists('notice', 'idx_notice_status_expire', 'status, expire_time');
DROP PROCEDURE IF EXISTS add_index_if_not_exists;
