-- =====================================================
-- UEAS 数据库索引优化脚本
-- =====================================================
-- 为常用查询字段添加索引，提升查询性能。
-- 执行前请确保对应表已创建。

-- 学生选课表索引
ALTER TABLE student_course ADD INDEX IF NOT EXISTS idx_student_semester (student_id, semester_id);
ALTER TABLE student_course ADD INDEX IF NOT EXISTS idx_course_offer (course_offer_id);

-- 开课表索引
ALTER TABLE course_offer ADD INDEX IF NOT EXISTS idx_teacher_semester (teacher_id, semester_id);

-- 公告表索引
ALTER TABLE notice ADD INDEX IF NOT EXISTS idx_publisher_status (publisher_id, status);

-- 角色菜单关联表索引
ALTER TABLE sys_role_menu ADD INDEX IF NOT EXISTS idx_role_menu (role_id, menu_id);
