-- =====================================================
-- UEAS 数据库初始化数据
-- =====================================================
-- 本脚本在 01-schema.sql 执行后运行，插入初始数据：
-- - 管理员账号（admin, jiaowu, teacher01, student01）
-- - 角色定义（ROLE_ADMIN, ROLE_ACADEMIC, ROLE_TEACHER, ROLE_STUDENT）
-- - 用户角色关联
-- - 学院、专业、班级等基础数据
-- - 课程和学期数据

-- 管理员密码 (SHA-256)
INSERT INTO sys_user (username, password, real_name, user_type, status) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '系统管理员', 3, 1),
('jiaowu', '8030975cdaead77e5ce51313222b307ac65509caf387207391e76919adf3b198', '教务管理员', 3, 1),
('teacher01', 'cde383eee8ee7a4400adf7a15f716f179a2eb97646b37e089eb8d6d04e663416', '张教师', 2, 1),
('student01', '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', '李学生', 1, 1);

-- 角色
INSERT INTO sys_role (name, code, sort) VALUES
('系统管理员', 'ROLE_ADMIN', 1),
('教务管理员', 'ROLE_ACADEMIC', 2),
('教师', 'ROLE_TEACHER', 3),
('学生', 'ROLE_STUDENT', 4);

-- 用户角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4);

-- 学院
INSERT INTO college (name, code, dean, phone) VALUES
('计算机科学与技术学院', 'CS', '王院长', '010-12345001'),
('数学与统计学院', 'MATH', '刘院长', '010-12345002'),
('外国语学院', 'FL', '陈院长', '010-12345003'),
('经济管理学院', 'EM', '赵院长', '010-12345004');

-- 专业
INSERT INTO major (name, code, college_id, degree, years, total_credits) VALUES
('计算机科学与技术', 'CS01', 1, '工学学士', 4, 160.0),
('软件工程', 'SE01', 1, '工学学士', 4, 155.0),
('数据科学与大数据技术', 'DS01', 1, '工学学士', 4, 158.0),
('数学与应用数学', 'MA01', 2, '理学学士', 4, 162.0),
('统计学', 'ST01', 2, '理学学士', 4, 158.0),
('英语', 'EN01', 3, '文学学士', 4, 160.0),
('工商管理', 'BA01', 4, '管理学学士', 4, 155.0),
('会计学', 'AC01', 4, '管理学学士', 4, 158.0);

-- 班级
INSERT INTO class_info (name, code, major_id, grade, student_count) VALUES
('计科2024-1班', 'CS202401', 1, '2024', 45),
('计科2024-2班', 'CS202402', 1, '2024', 42),
('软工2024-1班', 'SE202401', 2, '2024', 40),
('数应2024-1班', 'MA202401', 4, '2024', 38),
('英语2024-1班', 'EN202401', 6, '2024', 35),
('工商2024-1班', 'BA202401', 7, '2024', 43);

-- 学生
INSERT INTO student (user_id, student_no, name, gender, birth_date, id_card, college_id, major_id, class_id, grade, enroll_date) VALUES
(4, '2024001', '李学生', 1, '2005-03-15', '110101200503150011', 1, 1, 1, '2024', '2024-09-01'),
(4, '2024002', '王同学', 2, '2005-07-20', '110101200507200022', 1, 1, 1, '2024', '2024-09-01'),
(4, '2024003', '张三', 1, '2005-01-10', '110101200501100033', 1, 2, 3, '2024', '2024-09-01');

-- 教师
INSERT INTO teacher (user_id, teacher_no, name, gender, college_id, title, phone, email, office) VALUES
(3, 'T001', '张教师', 1, 1, '教授', '13800138001', 'zhang@univ.edu.cn', '理科楼A301'),
(3, 'T002', '李教授', 2, 2, '副教授', '13800138002', 'li@univ.edu.cn', '文科楼B205');

-- 课程
INSERT INTO course (course_no, name, english_name, college_id, category, credits, total_hours, theory_hours, practice_hours, exam_type, max_students) VALUES
('CS101', '高等数学', 'Advanced Mathematics', 2, 1, 5.0, 80, 64, 16, 1, 120),
('CS102', '程序设计基础', 'Programming Fundamentals', 1, 1, 4.0, 64, 48, 16, 1, 60),
('CS103', '数据结构', 'Data Structures', 1, 1, 4.0, 64, 48, 16, 1, 60),
('CS104', '操作系统', 'Operating Systems', 1, 1, 4.0, 64, 48, 16, 1, 60),
('CS105', '计算机网络', 'Computer Networks', 1, 1, 3.0, 48, 36, 12, 1, 60),
('FL101', '大学英语', 'College English', 3, 3, 2.0, 32, 24, 8, 2, 80),
('EM101', '管理学原理', 'Management Principles', 4, 2, 3.0, 48, 36, 12, 2, 60),
('CS106', '数据库原理', 'Database Principles', 1, 1, 4.0, 64, 48, 16, 1, 60);

-- 学期
INSERT INTO semester (name, code, start_date, end_date, select_start, select_end, status) VALUES
('2025-2026学年第一学期', '2025-1', '2025-09-01', '2026-01-15', '2025-08-25 08:00:00', '2025-09-05 18:00:00', 2),
('2025-2026学年第二学期', '2025-2', '2026-02-20', '2026-06-30', NULL, NULL, 0);

-- 开课
INSERT INTO course_offer (course_id, teacher_id, semester_id, classroom, schedule, weeks, enrolled, max_students, status) VALUES
(2, 1, 1, '教学楼A101', '周一3-4节,周三1-2节', '1-16周', 35, 60, 1),
(3, 1, 1, '教学楼A202', '周二5-6节,周四3-4节', '1-16周', 28, 60, 1),
(1, 2, 1, '理科楼B101', '周一1-2节,周五3-4节', '1-16周', 40, 120, 1),
(5, 1, 1, '机房C301', '周三5-6节,周五1-2节', '1-16周', 20, 60, 1),
(8, 1, 1, '机房C202', '周四3-4节,周五5-6节', '1-16周', 15, 60, 1);

-- 菜单权限
INSERT INTO sys_menu (parent_id, name, path, component, icon, type, permission, sort, visible, status) VALUES
(0, '首页', '/dashboard', 'dashboard/index', 'House', 1, '', 1, 1, 1),
(0, '基础管理', '/basic', NULL, 'School', 0, '', 10, 1, 1),
(2, '学院管理', '/basic/college', 'basic/college/index', '', 1, 'basic:college:query', 1, 1, 1),
(2, '专业管理', '/basic/major', 'basic/major/index', '', 1, 'basic:major:query', 2, 1, 1),
(2, '班级管理', '/basic/class', 'basic/class/index', '', 1, 'basic:class:query', 3, 1, 1),
(0, '学生管理', '/student', NULL, 'User', 0, '', 20, 1, 1),
(6, '学生列表', '/student/list', 'basic/student/index', '', 1, 'student:query', 1, 1, 1),
(0, '教师管理', '/teacher', NULL, 'Avatar', 0, '', 30, 1, 1),
(8, '教师列表', '/teacher/list', 'basic/teacher/index', '', 1, 'teacher:query', 1, 1, 1),
(0, '教务管理', '/program', NULL, 'Document', 0, '', 40, 1, 1),
(10, '课程管理', '/program/course', 'program/course/index', '', 1, 'program:course:query', 1, 1, 1),
(10, '开课管理', '/program/offering', 'program/offering/index', '', 1, 'program:offering:query', 2, 1, 1),
(10, '培养方案', '/program/plan', 'program/plan/index', '', 1, 'program:plan:query', 3, 1, 1),
(0, '排课管理', '/schedule', NULL, 'Calendar', 0, '', 50, 1, 1),
(14, '手动排课', '/schedule/manual', 'schedule/manual/index', '', 1, 'schedule:manual', 1, 1, 1),
(14, '自动排课', '/schedule/auto', 'schedule/auto/index', '', 1, 'schedule:auto', 2, 1, 1),
(14, '调课申请', '/schedule/adjust', 'schedule/adjust/index', '', 1, 'schedule:adjust', 3, 1, 1),
(14, '开课列表', '/schedule/offering', 'schedule/offering/index', '', 1, 'schedule:offering', 4, 1, 1),
(0, '选课管理', '/enrollment', NULL, 'Select', 0, '', 60, 1, 1),
(20, '选课批次', '/enrollment/batch', 'enrollment/batch/index', '', 1, 'enrollment:batch', 1, 1, 1),
(20, '我的选课', '/enrollment/my', 'enrollment/my/index', '', 1, 'enrollment:my', 2, 1, 1),
(20, '选课名单', '/enrollment/roster', 'enrollment/roster/index', '', 1, 'enrollment:roster', 3, 1, 1),
(20, '选课选择', '/enrollment/select', 'enrollment/select/index', '', 1, 'enrollment:select', 4, 1, 1),
(0, '成绩管理', '/score', NULL, 'DataAnalysis', 0, '', 70, 1, 1),
(25, '成绩录入', '/score/input', 'score/input/index', '', 1, 'score:input', 1, 1, 1),
(25, '成绩审核', '/score/approve', 'score/approve/index', '', 1, 'score:approve', 2, 1, 1),
(25, '成绩发布', '/score/publish', 'score/publish/index', '', 1, 'score:publish', 3, 1, 1),
(25, '成绩修改', '/score/change', 'score/change/index', '', 1, 'score:change', 4, 1, 1),
(25, '我的成绩', '/score/my', 'score/my/index', '', 1, 'score:my', 5, 1, 1),
(25, 'GPA计算', '/score/gpa', 'score/gpa/index', '', 1, 'score:gpa', 6, 1, 1),
(0, '考试管理', '/exam', NULL, 'EditPen', 0, '', 80, 1, 1),
(31, '考试安排', '/exam/arrange', 'exam/arrange/index', '', 1, 'exam:arrange', 1, 1, 1),
(31, '考场分配', '/exam/room', 'exam/room/index', '', 1, 'exam:room', 2, 1, 1),
(31, '监考安排', '/exam/invigilate', 'exam/invigilate/index', '', 1, 'exam:invigilate', 3, 1, 1),
(0, '教学评价', '/evaluation', NULL, 'Star', 0, '', 90, 1, 1),
(36, '问卷调查', '/evaluation/questionnaire', 'evaluation/questionnaire/index', '', 1, 'evaluation:questionnaire', 1, 1, 1),
(36, '教学评价', '/evaluation/submit', 'evaluation/submit/index', '', 1, 'evaluation:submit', 2, 1, 1),
(36, '评价统计', '/evaluation/statistics', 'evaluation/statistics/index', '', 1, 'evaluation:statistics', 3, 1, 1),
(0, '数据统计', '/report', NULL, 'Chart', 0, '', 100, 1, 1),
(40, '选课统计', '/report/enrollment', 'report/enrollment/index', '', 1, 'report:enrollment', 1, 1, 1),
(40, '成绩统计', '/report/score', 'report/score/index', '', 1, 'report:score', 2, 1, 1),
(40, '教师统计', '/report/teacher', 'report/teacher/index', '', 1, 'report:teacher', 3, 1, 1),
(0, '系统管理', '/system', NULL, 'Setting', 0, '', 200, 1, 1),
(45, '用户管理', '/system/user', 'system/user/index', '', 1, 'system:user:query', 1, 1, 1),
(45, '角色管理', '/system/role', 'system/role/index', '', 1, 'system:role:query', 2, 1, 1),
(45, '菜单管理', '/system/menu', 'system/menu/index', '', 1, 'system:menu:query', 3, 1, 1),
(45, '数据字典', '/system/dict', 'system/dict/index', '', 1, 'system:dict:query', 4, 1, 1),
(45, '操作日志', '/system/log', 'system/log/index', '', 1, 'system:log:query', 5, 1, 1);

-- 角色菜单授权
-- 管理员(10): 所有菜单
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu;
-- 教务管理员(2): 基础管理+教务+排课+选课+成绩+考试+评价+统计
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 2, id FROM sys_menu WHERE parent_id IN (0, 2, 6, 8, 10, 14, 20, 25, 31, 36, 40);
-- 教师(3): 我的选课+成绩录入+我的成绩+教学评价
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 3, id FROM sys_menu WHERE path IN ('/enrollment/my', '/score/input', '/score/my', '/evaluation/submit', '/dashboard');
-- 学生(4): 首页+我的选课+我的成绩+教学评价
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 4, id FROM sys_menu WHERE path IN ('/dashboard', '/enrollment/my', '/score/my', '/evaluation/submit', '/exam/arrange');

-- 数据字典
INSERT INTO dict_data (dict_type, label, value, sort) VALUES
('user_type', '学生', '1', 1),
('user_type', '教师', '2', 2),
('user_type', '管理员', '3', 3),
('gender', '男', '1', 1),
('gender', '女', '2', 2),
('course_category', '必修', '1', 1),
('course_category', '选修', '2', 2),
('course_category', '公选', '3', 3),
('course_category', '实践', '4', 4),
('exam_type', '考试', '1', 1),
('exam_type', '考查', '2', 2),
('notice_type', '系统公告', '1', 1),
('notice_type', '教务通知', '2', 2),
('notice_type', '选课通知', '3', 3),
('student_status', '在读', '1', 1),
('student_status', '休学', '2', 2),
('student_status', '退学', '3', 3),
('student_status', '毕业', '4', 4);

-- 初始公告
INSERT INTO notice (title, content, type, publisher_id, target_role, top_flag, status, publish_time) VALUES
('欢迎使用大学教务管理系统', '<p>系统已正式上线，请各位师生合理使用。</p>', 1, 1, '', 1, 1, NOW()),
('2025-2026学年第一学期选课通知', '<p>请同学们在规定时间内完成选课，逾期系统将关闭选课通道。</p>', 3, 2, 'ROLE_STUDENT', 1, 1, NOW());
