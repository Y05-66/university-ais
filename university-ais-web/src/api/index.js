import request from '@/api/request'

/**
 * 业务 API 接口统一导出。
 *
 * <p>按模块分类：
 * <ul>
 *   <li>系统管理 — 用户、角色、菜单、日志、字典</li>
 *   <li>基础管理 — 学院、专业、班级、学期、学生、教师</li>
 *   <li>教务管理 — 课程、开课</li>
 *   <li>选课管理 — 选课、退课、授课列表</li>
 *   <li>成绩管理 — 录入、查询、审核</li>
 *   <li>考试管理 — 安排、考场、监考</li>
 *   <li>教学评价 — 问卷、提交</li>
 *   <li>通知公告</li>
 * </ul>
 */

// ========== 系统管理 ==========

/** 分页查询用户列表 */
export function getUserPage(params) {
  return request.get('/sys-user/page', { params })
}

/** 新增用户 */
export function createUser(data) {
  return request.post('/sys-user', data)
}

/** 修改用户信息 */
export function updateUser(data) {
  return request.put('/sys-user', data)
}

/** 删除用户 */
export function deleteUser(id) {
  return request.delete(`/sys-user/${id}`)
}

/** 重置用户密码 */
export function resetPassword(id, newPassword) {
  return request.put(`/sys-user/${id}/password`, { newPassword })
}

/** 获取所有可用用户列表 */
export function getUserList() {
  return request.get('/sys-user/list')
}

/** 查询角色列表（含权限） */
export function getRoleList() {
  return request.get('/sys-role/list')
}

/** 获取角色的菜单 ID 列表 */
export function getRoleMenus(roleId) {
  return request.get(`/sys-role/${roleId}/menus`)
}

/** 设置角色的菜单权限 */
export function setRoleMenus(roleId, menuIds) {
  return request.put(`/sys-role/${roleId}/menus`, menuIds)
}

/** 新增角色 */
export function addRole(data) {
  return request.post('/sys-role', data)
}

/** 修改角色 */
export function updateRole(data) {
  return request.put('/sys-role', data)
}

/** 删除角色 */
export function deleteRole(id) {
  return request.delete(`/sys-role/${id}`)
}

/** 查询菜单树 */
export function getMenuTree() {
  return request.get('/sys-menu/tree')
}

/** 根据角色 ID 查询菜单树 */
export function getMenuTreeByRole(roleId) {
  return request.get(`/sys-menu/tree/${roleId}`)
}

/** 新增菜单 */
export function addMenu(data) {
  return request.post('/sys-menu', data)
}

/** 修改菜单 */
export function updateMenu(data) {
  return request.put('/sys-menu', data)
}

/** 删除菜单 */
export function deleteMenu(id) {
  return request.delete(`/sys-menu/${id}`)
}

/** 分页查询操作日志 */
export function getLogPage(params) {
  return request.get('/sys-log/page', { params })
}

/** 清空操作日志 */
export function clearLogs() {
  return request.delete('/sys-log/clear')
}

/** 根据字典类型查询字典数据 */
export function getDictByType(dictType) {
  return request.get(`/sys-dict/type/${dictType}`)
}

/** 查询所有字典数据 */
export function getDictList() {
  return request.get('/sys-dict/list')
}

// ========== 基础管理 ==========

/** 查询学院列表 */
export function getCollegeList() {
  return request.get('/basic/college/list')
}

/** 新增学院 */
export function addCollege(data) {
  return request.post('/basic/college', data)
}

/** 修改学院 */
export function updateCollege(data) {
  return request.put('/basic/college', data)
}

/** 删除学院 */
export function deleteCollege(id) {
  return request.delete(`/basic/college/${id}`)
}

/** 分页查询专业 */
export function getMajorPage(params) {
  return request.get('/basic/major/page', { params })
}

/** 新增专业 */
export function addMajor(data) {
  return request.post('/basic/major', data)
}

/** 修改专业 */
export function updateMajor(data) {
  return request.put('/basic/major', data)
}

/** 删除专业 */
export function deleteMajor(id) {
  return request.delete(`/basic/major/${id}`)
}

/** 查询班级列表（下拉框用） */
export function getClassList(params) {
  return request.get('/basic/class/list', { params })
}

/** 分页查询班级列表 */
export function getClassPage(params) {
  return request.get('/basic/class/page', { params })
}

/** 新增班级 */
export function addClass(data) {
  return request.post('/basic/class', data)
}

/** 修改班级 */
export function updateClass(data) {
  return request.put('/basic/class', data)
}

/** 删除班级 */
export function deleteClass(id) {
  return request.delete(`/basic/class/${id}`)
}

/** 查询学期列表 */
export function getSemesterList() {
  return request.get('/basic/semester/list')
}

/** 分页查询学生 */
export function getStudentPage(params) {
  return request.get('/student/page', { params })
}

/** 查询学生详情 */
export function getStudentDetail(id) {
  return request.get(`/student/${id}`)
}

/** 新增学生 */
export function addStudent(data) {
  return request.post('/student', data)
}

/** 修改学生 */
export function updateStudent(data) {
  return request.put('/student', data)
}

/** 删除学生 */
export function deleteStudent(id) {
  return request.delete(`/student/${id}`)
}

/** 分页查询教师 */
export function getTeacherPage(params) {
  return request.get('/teacher/page', { params })
}

/** 查询教师列表（下拉框用） */
export function getTeachers(params) {
  return request.get('/teacher/list', { params })
}

/** 查询学生列表（下拉框用） */
export function getStudents(params) {
  return request.get('/student/list', { params })
}

/** 新增教师 */
export function addTeacher(data) {
  return request.post('/teacher', data)
}

/** 修改教师 */
export function updateTeacher(data) {
  return request.put('/teacher', data)
}

/** 删除教师 */
export function deleteTeacher(id) {
  return request.delete(`/teacher/${id}`)
}

// ========== 教务管理 ==========

/** 分页查询课程 */
export function getCoursePage(params) {
  return request.get('/course/page', { params })
}

/** 查询课程列表（下拉框用） */
export function getCourses(params) {
  return request.get('/course/list', { params })
}

/** 新增课程 */
export function addCourse(data) {
  return request.post('/course', data)
}

/** 修改课程 */
export function updateCourse(data) {
  return request.put('/course', data)
}

/** 删除课程 */
export function deleteCourse(id) {
  return request.delete(`/course/${id}`)
}

/** 分页查询开课列表 */
export function getCourseOfferPage(params) {
  return request.get('/course-offer/page', { params })
}

/** 查询开课列表（下拉框用） */
export function getCourseOfferList(params) {
  return request.get('/course-offer/list', { params })
}

/** 新增开课 */
export function addCourseOffer(data) {
  return request.post('/course-offer', data)
}

/** 修改开课 */
export function updateCourseOffer(data) {
  return request.put('/course-offer', data)
}

/** 删除开课 */
export function deleteCourseOffer(id) {
  return request.delete(`/course-offer/${id}`)
}

// ========== 选课管理 ==========

/** 查询当前用户已选课程 */
export function getEnrollmentMy(params) {
  return request.get('/enrollment/my', { params })
}

/** 查询可选课程列表 */
export function getAvailableCourses(semesterId) {
  return request.get('/enrollment/available', { params: { semesterId } })
}

/** 获取抢课开关状态（仅管理员可设置） */
export function getEnrollmentEnabled() {
  return request.get('/sys-config/enrollment/enabled')
}

/** 设置抢课开关（仅管理员） */
export function setEnrollmentEnabled(enabled) {
  return request.put('/sys-config/enrollment/enabled', null, { params: { enabled } })
}

/** 选择课程 */
export function selectCourse(courseOfferId) {
  return request.post(`/enrollment/select/${courseOfferId}`)
}

/** 退选课程 */
export function dropCourse(courseOfferId) {
  return request.post(`/enrollment/drop/${courseOfferId}`)
}

/** 查询选课名单 */
export function getEnrollmentRoster(courseOfferId) {
  return request.get(`/enrollment/roster/${courseOfferId}`)
}

/** 查询教师授课列表 */
export function getMyTeachingCourses(params) {
  return request.get('/course-offer/my', { params })
}

/** 查询教师课表（按星期节次展开） */
export function getMyTimetable() {
  return request.get('/course-offer/timetable')
}

/** 查询指定教师或学生的课表（管理员用） */
export function getTimetableByTeacher(teacherId, semesterId) {
  const params = {}
  if (teacherId) params.teacherId = teacherId
  if (semesterId) params.semesterId = semesterId
  return request.get('/course-offer/timetable-query', { params })
}

/** 查询指定学生的课表（管理员用） */
export function getTimetableByStudent(studentId, semesterId) {
  const params = {}
  if (studentId) params.studentId = studentId
  if (semesterId) params.semesterId = semesterId
  return request.get('/course-offer/timetable-query', { params })
}

// ========== 成绩管理 ==========

/** 查询学生本人成绩 */
export function getMyScores(params) {
  return request.get('/score/my', { params })
}

/** 查询教师所教课程成绩（含学生） */
export function getTeacherScores(params) {
  return request.get('/score/teacher', { params })
}

/** 录入/更新成绩 */
export function inputScore(data) {
  return request.post('/score/input', data)
}

/** 查询学员成绩录入列表（学员中心视图） */
export function getStudentScoreInputList(params) {
  return request.get('/score/student/input/list', { params })
}

/** 审核成绩 */
export function approveScore(id) {
  return request.put(`/score/${id}/approve`)
}

/** 修改成绩 */
export function changeScore(id, data) {
  return request.put(`/score/${id}/change`, data)
}

/** 发布成绩通知 */
export function publishScore(id) {
  return request.put(`/score/${id}/publish`)
}

// ========== 考试管理 ==========

/** 分页查询考试安排 */
export function getExamPage(params) {
  return request.get('/exam/page', { params })
}

/** 新增考试安排 */
export function addExam(data) {
  return request.post('/exam', data)
}

/** 修改考试安排 */
export function updateExam(data) {
  return request.put('/exam', data)
}

/** 删除考试安排 */
export function deleteExam(id) {
  return request.delete(`/exam/${id}`)
}

/** 分页查询考场 */
export function getExamRoomPage(params) {
  return request.get('/exam/room/page', { params })
}

/** 新增考场 */
export function addExamRoom(data) {
  return request.post('/exam/room', data)
}

/** 修改考场 */
export function updateExamRoom(data) {
  return request.put('/exam/room', data)
}

/** 删除考场 */
export function deleteExamRoom(id) {
  return request.delete(`/exam/room/${id}`)
}

/** 分页查询监考安排 */
export function getExamInvigilatePage(params) {
  return request.get('/exam/invigilate/page', { params })
}

/** 新增监考安排 */
export function addExamInvigilate(data) {
  return request.post('/exam/invigilate', data)
}

/** 修改监考安排 */
export function updateExamInvigilate(data) {
  return request.put('/exam/invigilate', data)
}

/** 删除监考安排 */
export function deleteExamInvigilate(id) {
  return request.delete(`/exam/invigilate/${id}`)
}

// ========== 教学评价 ==========

/** 分页查询评价问卷 */
export function getEvaluationPage(params) {
  return request.get('/evaluation/questionnaire/page', { params })
}

/** 新增评价问卷 */
export function addEvaluation(data) {
  return request.post('/evaluation/questionnaire', data)
}

/** 提交评价 */
export function submitEvaluation(data) {
  return request.post('/evaluation/submit', data)
}

/** 删除评价问卷 */
export function deleteEvaluation(id) {
  return request.delete(`/evaluation/questionnaire/${id}`)
}

// ========== 通知公告 ==========

/** 分页查询公告 */
export function getNoticePage(params) {
  return request.get('/notice/page', { params })
}

/** 查询公告详情 */
export function getNoticeDetail(id) {
  return request.get(`/notice/${id}`)
}

/** 新增公告 */
export function addNotice(data) {
  return request.post('/notice', data)
}

/** 修改公告 */
export function updateNotice(data) {
  return request.put('/notice', data)
}

/** 删除公告 */
export function deleteNotice(id) {
  return request.delete(`/notice/${id}`)
}


// ========== 数据看板 ==========

/** 获取首页统计数据 */
export function getDashboardStats() {
  return request.get('/dashboard/stats')
}

// ==================== AI 智能助手接口 ====================

/**
 * 通用智能问答
 * @param {string} question - 问题
 * @param {string} context - 可选的业务上下文
 */
export function aiChat(question, context) {
  return request.post('/ai/chat', { question, context })
}

/**
 * 学生智能问答
 * @param {string} question - 问题
 * @param {string} context - 可选的业务上下文
 */
export function aiChatForStudent(question, context) {
  return request.post('/ai/chat/student', { question, context })
}

/**
 * 教师智能问答
 * @param {string} question - 问题
 * @param {string} context - 可选的业务上下文
 */
export function aiChatForTeacher(question, context) {
  return request.post('/ai/chat/teacher', { question, context })
}

/**
 * 管理员智能问答
 * @param {string} question - 问题
 * @param {string} context - 可选的业务上下文
 */
export function aiChatForAdmin(question, context) {
  return request.post('/ai/chat/admin', { question, context })
}

/**
 * 获取当前教师的监考安排（含考试详情）。
 */
export function getMyInvigilation() {
  return request.get('/exam/invigilate/my')
}


/**
 * 获取当前用户的考试安排（学生看自己的考试，教师看自己监考的考试）。
 */
export function getMyExams() {
  return request.get('/exam/my')
}

/**
 * 获取当前用户的考试安排（学生看自己的考试，教师看自己监考的考试）。
 */
