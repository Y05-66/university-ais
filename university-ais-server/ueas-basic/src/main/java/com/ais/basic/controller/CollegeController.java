/**
 * 基础数据管理控制器。
 *
 * <p>提供学院、专业、班级、课程、学期等基础数据的CRUD操作。
 * 所有接口路径以 /basic 开头。
 *
 * <h3>权限说明</h3>
 * <ul>
 *   <li>列表查询接口（/list、/page）大多已配置为免登录访问（SaIgnore）</li>
 *   <li>增删改接口需要登录权限</li>
 * </ul>
 */
package com.ais.basic.controller;

import com.ais.basic.entity.College;
import com.ais.basic.service.CollegeService;
import com.ais.common.result.PageResult;
import com.ais.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 学院管理控制器。
 *
 * <p>提供学院的增删改查接口，路径前缀 /basic/college。
 */
@Tag(name = "基础管理-学院")
@RestController
@RequestMapping("/basic/college")
@RequiredArgsConstructor
@Slf4j
public class CollegeController {

    /** 学院服务 */
    private final CollegeService collegeService;

    /**
     * 查询学院列表（下拉框用）。
     *
     * @return 学院列表
     */
    @Operation(summary = "查询学院列表")
    @GetMapping("/list")
    public R<List<College>> list() {
        return R.ok(collegeService.listAll());
    }

    /**
     * 分页查询学院。
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param params  查询条件
     * @return 分页结果
     */
    @Operation(summary = "分页查询学院")
    @GetMapping("/page")
    public R<PageResult<College>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            Map<String, Object> params) {
        return R.ok(collegeService.pageQuery(params, current, size));
    }

    /**
     * 新增学院。
     *
     * @param college 学院信息
     * @return 操作结果
     */
    @Operation(summary = "新增学院")
    @PostMapping
    public R<Void> add(@RequestBody College college) {
        log.info("新增学院: name={}", college.getName()); collegeService.save(college);
        return R.ok();
    }

    /**
     * 更新学院。
     *
     * @param college 学院信息（需包含id）
     * @return 操作结果
     */
    @Operation(summary = "更新学院")
    @PutMapping
    public R<Void> update(@RequestBody College college) {
        log.info("更新学院: id={}", college.getId()); collegeService.updateById(college);
        return R.ok();
    }

    /**
     * 删除学院。
     *
     * @param id 学院ID
     * @return 操作结果
     */
    @Operation(summary = "删除学院")
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        log.info("删除学院: id={}", id); collegeService.removeById(id);
        return R.ok();
    }
}
