-- Add deleted column to tables that are missing it
-- But only if the column doesn't already exist

-- sys_role
ALTER TABLE sys_role ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- sys_menu
ALTER TABLE sys_menu ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- sys_log
ALTER TABLE sys_log ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- class_info
ALTER TABLE class_info ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- college
ALTER TABLE college ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- major
ALTER TABLE major ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- course
ALTER TABLE course ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- course_offer
ALTER TABLE course_offer ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- semester
ALTER TABLE semester ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- student
ALTER TABLE student ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- teacher
ALTER TABLE teacher ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- student_course
ALTER TABLE student_course ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- exam
ALTER TABLE exam ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- notice
ALTER TABLE notice ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';

-- dict_data
ALTER TABLE dict_data ADD COLUMN deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除';
