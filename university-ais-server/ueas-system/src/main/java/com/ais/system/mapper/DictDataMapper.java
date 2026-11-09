package com.ais.system.mapper;

import com.ais.system.entity.DictData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 字典数据数据访问对象。
 *
 * <p>提供字典数据表的 CRUD 操作，以及按字典类型查询的功能。
 */
@Mapper
public interface DictDataMapper extends BaseMapper<DictData> {

    /**
     * 根据字典类型查询字典数据列表。
     *
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    List<DictData> selectByType(String dictType);
}
