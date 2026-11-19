package com.wms.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.wms.entity.Goods;
import com.wms.entity.Record;
import com.wms.entity.RecordRes;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wms
 * @since 2024-10-16
 */
@Mapper
public interface RecordMapper extends BaseMapper<Record> {
    IPage pageCC(IPage<Record> page, @Param(Constants.WRAPPER) Wrapper wrapper);

    /**
     * 按药品名/药品 id 查询流转流水（带出药品名、药房名、分类名、操作人、取药人）
     *
     * @param name    药品名，为空不过滤
     * @param goodsId 药品 id，为空不过滤
     * @param userId  取药人 id，普通用户查询自己的流水时传入
     */
    List<RecordRes> listFlow(@Param("name") String name,
                             @Param("goodsId") Integer goodsId,
                             @Param("userId") Integer userId);
}
