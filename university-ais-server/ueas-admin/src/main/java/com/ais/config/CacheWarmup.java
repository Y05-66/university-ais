package com.ais.config;

import com.ais.basic.entity.*;
import com.ais.basic.service.*;
import com.ais.common.config.CacheManager;
import com.ais.system.entity.DictData;
import com.ais.system.mapper.DictDataMapper;
import com.ais.system.mapper.SysRoleMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动预热组件。
 *
 * <p>在应用启动完成后（{@code CommandLineRunner}），将高频只读数据提前加载到 Redis 缓存中。
 * 这样可以避免首次请求时的缓存穿透问题，同时减轻数据库压力。
 */
@Component
public class CacheWarmup implements CommandLineRunner {

    private final CacheManager cacheManager;
    private final CollegeService collegeService;
    private final SemesterService semesterService;
    private final MajorService majorService;
    private final ClassInfoService classInfoService;
    private final CourseService courseService;
    private final TeacherService teacherService;
    private final DictDataMapper dictDataMapper;
    private final SysRoleMapper roleMapper;

    public CacheWarmup(CacheManager cacheManager,
                       CollegeService collegeService,
                       SemesterService semesterService,
                       MajorService majorService,
                       ClassInfoService classInfoService,
                       CourseService courseService,
                       TeacherService teacherService,
                       DictDataMapper dictDataMapper,
                       SysRoleMapper roleMapper) {
        this.cacheManager = cacheManager;
        this.collegeService = collegeService;
        this.semesterService = semesterService;
        this.majorService = majorService;
        this.classInfoService = classInfoService;
        this.courseService = courseService;
        this.teacherService = teacherService;
        this.dictDataMapper = dictDataMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public void run(String... args) {
        System.out.println("[CacheWarmup] 开始预热数据缓存...");
        warmup();
        System.out.println("[CacheWarmup] 数据缓存预热完成");
    }

    private void warmup() {
        warmup("college:list", () -> collegeService.listAll());
        warmup("semester:list", () -> semesterService.listAll());
        warmup("major:list", () -> majorService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Major>()
                .eq(Major::getDeleted, 0).orderByAsc(Major::getId)));
        warmup("class:list", () -> classInfoService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ClassInfo>()
                .eq(ClassInfo::getDeleted, 0).orderByAsc(ClassInfo::getId)));
        warmup("course:list", () -> courseService.listAll());
        warmup("teacher:list", () -> teacherService.listAll());
        warmup("dict:list", () -> dictDataMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<DictData>()
                .eq(DictData::getDeleted, 0)));
        warmup("role:list", () -> roleMapper.selectRolesWithPermissions());
    }

    @SuppressWarnings("unchecked")
    private <T> void warmup(String key, java.util.function.Supplier<List<T>> loader) {
        try {
            List<T> data = loader.get();
            if (data != null && !data.isEmpty()) {
                cacheManager.set(key, data, CacheManager.AUTH_TTL);
                System.out.println("[CacheWarmup] 预热缓存: ueas:cache:" + key + " (" + data.size() + " 条)");
            }
        } catch (Exception e) {
            System.err.println("[CacheWarmup] 预热缓存失败: ueas:cache:" + key + " — " + e.getMessage());
        }
    }
}
