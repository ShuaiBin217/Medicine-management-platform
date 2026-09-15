package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.Goods;
import com.wms.entity.Record;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wms
 * @since 2025-10-16
 */
public interface RecordService extends IService<Record> {

    IPage pageCC(IPage<Record> page, Wrapper wrapper);

    /**
     * 事务性保存流转记录并同步更新库存（含乐观锁防超卖）
     * @param record 流转记录
     * @return 操作结果
     */
    boolean saveWithInventory(Record record);

    /**
     * 批量删除流转记录（含权限校验和操作日志）
     * @param ids 要删除的记录ID列表
     * @param operatorId 操作人ID
     * @param operatorRole 操作人角色
     * @return 删除的记录数
     */
    int batchDelete(List<Integer> ids, Integer operatorId, Integer operatorRole);

    /**
     * 预扣减库存（出库前预占库存，防止超卖）
     * @param record 流转记录
     * @return 预扣减记录ID
     */
    Integer preDeduct(Record record);

    /**
     * 确认预扣减（将预扣减状态改为已确认）
     * @param recordId 预扣减记录ID
     * @return 操作结果
     */
    boolean confirmDeduct(Integer recordId);

    /**
     * 回滚预扣减（超时未确认时恢复库存）
     * @param recordId 预扣减记录ID
     * @return 操作结果
     */
    boolean rollbackDeduct(Integer recordId);
}
