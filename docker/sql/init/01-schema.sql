-- =====================================================
-- UEAS 数据库初始化脚本 - 建表
-- =====================================================

CREATE TABLE IF NOT EXISTS `sys_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`    VARCHAR(50)  NOT NULL COMMENT '用户名(学号/工号)',
  `password`    VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
  `real_name`   VARCHAR(50)  NOT NULL COMMENT '真实姓名',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar`      VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `user_type`   TINYINT      NOT NULL COMMENT '用户类型: 1学生 2教师 3管理员',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
  `last_login`  DATETIME     DEFAULT NULL COMMENT '最后登录时间',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS `student` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT       NOT NULL COMMENT '关联用户ID',
  `student_no`    VARCHAR(20)  NOT NULL COMMENT '学号',
  `name`          VARCHAR(50)  NOT NULL COMMENT '姓名',
  `gender`        TINYINT      NOT NULL COMMENT '性别: 1男 2女',
  `birth_date`    DATE         DEFAULT NULL COMMENT '出生日期',
  `id_card`       VARCHAR(18)  DEFAULT NULL COMMENT '身份证号',
  `college_id`    BIGINT       NOT NULL COMMENT '学院ID',
  `major_id`      BIGINT       NOT NULL COMMENT '专业ID',
  `class_id`      BIGINT       NOT NULL COMMENT '班级ID',
  `grade`         VARCHAR(4)   NOT NULL COMMENT '年级 如2024',
  `enroll_date`   DATE         NOT NULL COMMENT '入学日期',
  `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '学籍状态: 1在读 2休学 3退学 4毕业',
  `gpa`           DECIMAL(3,2) DEFAULT NULL COMMENT 'GPA',
  `total_credits` DECIMAL(6,1) DEFAULT 0 COMMENT '已修学分',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_college_class` (`college_id`, `class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

CREATE TABLE IF NOT EXISTS `teacher` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT       NOT NULL COMMENT '关联用户ID',
  `teacher_no`   VARCHAR(20)  NOT NULL COMMENT '工号',
  `name`         VARCHAR(50)  NOT NULL COMMENT '姓名',
  `gender`       TINYINT      NOT NULL COMMENT '性别: 1男 2女',
  `college_id`   BIGINT       NOT NULL COMMENT '所属学院ID',
  `title`        VARCHAR(30)  DEFAULT NULL COMMENT '职称: 助教/讲师/副教授/教授',
  `phone`        VARCHAR(20)  DEFAULT NULL,
  `email`        VARCHAR(100) DEFAULT NULL,
  `office`       VARCHAR(100) DEFAULT NULL COMMENT '办公室',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0离校 1在职',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_teacher_no` (`teacher_no`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师信息表';

CREATE TABLE IF NOT EXISTS `college` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `name`         VARCHAR(100) NOT NULL COMMENT '学院名称',
  `code`         VARCHAR(20)  NOT NULL COMMENT '学院代码',
  `dean`         VARCHAR(50)  DEFAULT NULL COMMENT '院长',
  `phone`        VARCHAR(20)  DEFAULT NULL,
  `status`       TINYINT      NOT NULL DEFAULT 1,
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院表';

CREATE TABLE IF NOT EXISTS `major` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `name`         VARCHAR(100) NOT NULL COMMENT '专业名称',
  `code`         VARCHAR(20)  NOT NULL COMMENT '专业代码',
  `college_id`   BIGINT       NOT NULL COMMENT '所属学院ID',
  `degree`       VARCHAR(20)  DEFAULT NULL COMMENT '授予学位',
  `years`        TINYINT      NOT NULL DEFAULT 4 COMMENT '学制(年)',
  `total_credits` DECIMAL(6,1) NOT NULL DEFAULT 0 COMMENT '毕业总学分要求',
  `status`       TINYINT      NOT NULL DEFAULT 1,
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_college` (`college_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业表';

CREATE TABLE IF NOT EXISTS `class_info` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `name`         VARCHAR(100) NOT NULL COMMENT '班级名称 如: 计科2024-1班',
  `code`         VARCHAR(20)  NOT NULL COMMENT '班级代码',
  `major_id`     BIGINT       NOT NULL COMMENT '所属专业ID',
  `grade`        VARCHAR(4)   NOT NULL COMMENT '年级',
  `advisor_id`   BIGINT       DEFAULT NULL COMMENT '班主任/导师ID',
  `student_count` INT          NOT NULL DEFAULT 0 COMMENT '班级人数',
  `status`       TINYINT      NOT NULL DEFAULT 1,
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

CREATE TABLE IF NOT EXISTS `course` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `course_no`      VARCHAR(20)   NOT NULL COMMENT '课程编号',
  `name`           VARCHAR(100)  NOT NULL COMMENT '课程名称',
  `english_name`   VARCHAR(200)  DEFAULT NULL COMMENT '英文名称',
  `college_id`     BIGINT        NOT NULL COMMENT '开课学院ID',
  `category`       TINYINT       NOT NULL COMMENT '课程类别: 1必修 2选修 3公选 4实践',
  `credits`        DECIMAL(3,1)  NOT NULL COMMENT '学分',
  `total_hours`    INT           NOT NULL COMMENT '总学时',
  `theory_hours`   INT           DEFAULT NULL COMMENT '理论学时',
  `practice_hours` INT           DEFAULT NULL COMMENT '实践学时',
  `exam_type`      TINYINT       NOT NULL DEFAULT 1 COMMENT '考核方式: 1考试 2考查',
  `description`    TEXT          DEFAULT NULL COMMENT '课程简介',
  `prerequisite`   VARCHAR(500)  DEFAULT NULL COMMENT '先修课程编号(逗号分隔)',
  `max_students`   INT           DEFAULT 60 COMMENT '最大选课人数',
  `status`         TINYINT       NOT NULL DEFAULT 1,
  `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_course_no` (`course_no`),
  KEY `idx_college` (`college_id`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

CREATE TABLE IF NOT EXISTS `semester` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50) NOT NULL COMMENT '学期名称 如: 2025-2026学年第一学期',
  `code`        VARCHAR(20) NOT NULL COMMENT '学期代码 如: 2025-1',
  `start_date`  DATE        NOT NULL COMMENT '学期开始日期',
  `end_date`    DATE        NOT NULL COMMENT '学期结束日期',
  `select_start` DATETIME   DEFAULT NULL COMMENT '选课开始时间',
  `select_end`  DATETIME    DEFAULT NULL COMMENT '选课结束时间',
  `status`      TINYINT     NOT NULL DEFAULT 0 COMMENT '0未开始 1选课中 2教学中 3已结束',
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学期表';

CREATE TABLE IF NOT EXISTS `course_offer` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT,
  `course_id`    BIGINT      NOT NULL COMMENT '课程ID',
  `teacher_id`   BIGINT      NOT NULL COMMENT '授课教师ID',
  `semester_id`  BIGINT      NOT NULL COMMENT '学期ID',
  `classroom`    VARCHAR(50) DEFAULT NULL COMMENT '教室',
  `schedule`     VARCHAR(200) NOT NULL COMMENT '上课时间 如: 周一3-4节,周三1-2节',
  `weeks`        VARCHAR(100) NOT NULL COMMENT '上课周次 如: 1-16周',
  `enrolled`     INT         NOT NULL DEFAULT 0 COMMENT '已选人数',
  `max_students` INT         NOT NULL DEFAULT 60 COMMENT '容量上限',
  `status`       TINYINT     NOT NULL DEFAULT 1 COMMENT '状态: 1正常 2已满 3已取消',
  `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_course` (`course_id`),
  KEY `idx_teacher` (`teacher_id`),
  KEY `idx_semester` (`semester_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开课表';

CREATE TABLE IF NOT EXISTS `student_course` (
  `id`              BIGINT   NOT NULL AUTO_INCREMENT,
  `student_id`      BIGINT   NOT NULL COMMENT '学生ID',
  `course_offer_id` BIGINT   NOT NULL COMMENT '开课ID',
  `semester_id`     BIGINT   NOT NULL COMMENT '学期ID',
  `select_time`     DATETIME NOT NULL COMMENT '选课时间',
  `status`          TINYINT  NOT NULL DEFAULT 1 COMMENT '状态: 1已选 2已退 3已完',
  `score`           DECIMAL(5,2) DEFAULT NULL COMMENT '成绩(0-100)',
  `grade_point`     DECIMAL(3,2) DEFAULT NULL COMMENT '绩点',
  `score_status`    TINYINT  DEFAULT NULL COMMENT '成绩状态: NULL未录入 1正常 2补考 3重修',
  `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_offer` (`student_id`, `course_offer_id`),
  KEY `idx_semester` (`semester_id`),
  KEY `idx_course_offer` (`course_offer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课/成绩表';

CREATE TABLE IF NOT EXISTS `exam` (
  `id`               BIGINT      NOT NULL AUTO_INCREMENT,
  `course_offer_id`  BIGINT      NOT NULL COMMENT '开课ID',
  `exam_type`        TINYINT     NOT NULL COMMENT '考试类型: 1期末 2期中 3补考',
  `exam_date`        DATE        NOT NULL COMMENT '考试日期',
  `start_time`       TIME        NOT NULL COMMENT '开始时间',
  `end_time`         TIME        NOT NULL COMMENT '结束时间',
  `classroom`        VARCHAR(50) NOT NULL COMMENT '考场',
  `seat_count`       INT         NOT NULL COMMENT '座位数',
  `invigilator`      VARCHAR(200) DEFAULT NULL COMMENT '监考教师(逗号分隔)',
  `status`           TINYINT     NOT NULL DEFAULT 1 COMMENT '1待考 2进行中 3已结束',
  `create_time`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_course_offer` (`course_offer_id`),
  KEY `idx_date` (`exam_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试安排表';

CREATE TABLE IF NOT EXISTS `notice` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT,
  `title`        VARCHAR(200) NOT NULL COMMENT '标题',
  `content`      TEXT         NOT NULL COMMENT '内容(富文本)',
  `type`         TINYINT      NOT NULL COMMENT '类型: 1系统公告 2教务通知 3选课通知',
  `publisher_id` BIGINT       NOT NULL COMMENT '发布者ID',
  `target_role`  VARCHAR(50)  DEFAULT NULL COMMENT '目标角色(逗号分隔,空=全部)',
  `top_flag`     TINYINT      NOT NULL DEFAULT 0 COMMENT '是否置顶: 0否 1是',
  `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '0草稿 1已发布 2已撤回',
  `publish_time` DATETIME     DEFAULT NULL COMMENT '发布时间',
  `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_type_status` (`type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

CREATE TABLE IF NOT EXISTS `sys_role` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50)  NOT NULL COMMENT '角色名称',
  `code`        VARCHAR(50)  NOT NULL COMMENT '角色编码',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `status`      TINYINT      NOT NULL DEFAULT 1,
  `remark`      VARCHAR(200) DEFAULT NULL,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id`        BIGINT       NOT NULL AUTO_INCREMENT,
  `parent_id` BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单ID',
  `name`      VARCHAR(50)  NOT NULL COMMENT '菜单名称',
  `path`      VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
  `component` VARCHAR(200) DEFAULT NULL COMMENT '前端组件路径',
  `icon`      VARCHAR(100) DEFAULT NULL COMMENT '图标',
  `type`      TINYINT      NOT NULL COMMENT '类型: 0目录 1菜单 2按钮',
  `permission` VARCHAR(100) DEFAULT NULL COMMENT '权限标识 如: student:query',
  `sort`      INT          NOT NULL DEFAULT 0,
  `visible`   TINYINT      NOT NULL DEFAULT 1 COMMENT '是否可见',
  `status`    TINYINT      NOT NULL DEFAULT 1,
  `create_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单权限表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
  `id`      BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `role_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `id`      BIGINT NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT NOT NULL,
  `menu_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

CREATE TABLE IF NOT EXISTS `sys_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       DEFAULT NULL COMMENT '操作人ID',
  `username`    VARCHAR(50)  DEFAULT NULL,
  `operation`   VARCHAR(200) NOT NULL COMMENT '操作描述',
  `method`      VARCHAR(200) DEFAULT NULL COMMENT '请求方法',
  `params`      TEXT         DEFAULT NULL COMMENT '请求参数',
  `ip`          VARCHAR(50)  DEFAULT NULL,
  `duration`    INT          DEFAULT NULL COMMENT '耗时(ms)',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1成功 0失败',
  `error_msg`   TEXT         DEFAULT NULL,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

CREATE TABLE IF NOT EXISTS `dict_data` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `dict_type`   VARCHAR(50)  NOT NULL COMMENT '字典类型',
  `label`       VARCHAR(100) NOT NULL COMMENT '字典标签',
  `value`       VARCHAR(50)  NOT NULL COMMENT '字典值',
  `sort`        INT          NOT NULL DEFAULT 0 COMMENT '排序',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
  `remark`      VARCHAR(200) DEFAULT NULL,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据字典表';

CREATE TABLE IF NOT EXISTS `exam_room` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `classroom` VARCHAR(50) NOT NULL COMMENT '考场名称',
  `seat_count` INT NOT NULL COMMENT '座位数',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=使用中, 0=停用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_classroom` (`classroom`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考场表';

CREATE TABLE IF NOT EXISTS `exam_invigilate` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `exam_id` BIGINT NOT NULL COMMENT '考试ID',
  `teacher_id` BIGINT NOT NULL COMMENT '教师ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=已确认, 0=待确认',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` TINYINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_exam` (`exam_id`),
  KEY `idx_teacher` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监考安排表';
