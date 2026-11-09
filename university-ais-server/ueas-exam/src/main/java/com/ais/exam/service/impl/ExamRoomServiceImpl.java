package com.ais.exam.service.impl;

import com.ais.exam.entity.ExamRoom;
import com.ais.exam.mapper.ExamRoomMapper;
import com.ais.exam.service.ExamRoomService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 考场服务实现类。
 *
 * <p>提供考场的增删改查基础操作，继承 MyBatis-Plus 的 ServiceImpl。
 */
@Service
public class ExamRoomServiceImpl extends ServiceImpl<ExamRoomMapper, ExamRoom> implements ExamRoomService {
}
