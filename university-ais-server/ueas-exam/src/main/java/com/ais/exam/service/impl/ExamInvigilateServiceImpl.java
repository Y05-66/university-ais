package com.ais.exam.service.impl;

import com.ais.exam.entity.ExamInvigilate;
import com.ais.exam.mapper.ExamInvigilateMapper;
import com.ais.exam.service.ExamInvigilateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 监考安排服务实现类。
 *
 * <p>提供监考安排的基础增删改查操作，继承 MyBatis-Plus 的 ServiceImpl。
 */
@Service
public class ExamInvigilateServiceImpl extends ServiceImpl<ExamInvigilateMapper, ExamInvigilate> implements ExamInvigilateService {
}
