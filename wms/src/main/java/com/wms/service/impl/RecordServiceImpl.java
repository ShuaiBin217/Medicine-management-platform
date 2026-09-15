package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.Goods;
import com.wms.entity.Record;
import com.wms.mapper.GoodsMapper;
import com.wms.mapper.RecordMapper;
import com.wms.service.GoodsService;
import com.wms.service.RecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wms
 * @since 2025-10-16
 */
@Slf4j
@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {

    @Resource
    private RecordMapper recordMapper;

    @Resource
    private GoodsService goodsService;

    @Override
    public IPage pageCC(IPage<Record> page, Wrapper wrapper) {
        return recordMapper.pageCC(page,wrapper);
    }

    /**
     * 事务性保存流转记录并同步更新库存
     * 使用乐观锁（version字段）防止并发超卖
     * 两个操作（库存更新+流水插入）在同一事务内，保证原子性
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveWithInventory(Record record) {
        Goods goods = goodsService.getById(record.getGoods());
        if (goods == null) {
            throw new RuntimeException("药品不存在");
        }

        int n = record.getCount();
        // action: "1"=入库(采购), "2"=出库(发药)
        if ("2".equals(record.getAction())) {
            n = -n;
            record.setCount(n);
        }

        int newCount = goods.getCount() + n;
        // 出库时检查库存是否充足
        if (newCount < 0) {
            throw new RuntimeException("库存不足，当前库存：" + goods.getCount() + "，尝试出库：" + (-n));
        }

        goods.setCount(newCount);
        // updateById 会自动带上 version 条件，乐观锁失败时返回 false
        boolean updateSuccess = goodsService.updateById(goods);
        if (!updateSuccess) {
            throw new RuntimeException("并发操作冲突，请重试");
        }

        // 同一事务内插入流水记录
        return recordMapper.insert(record) > 0;
    }

    /**
     * 批量删除流转记录
     * 权限控制：仅超级管理员(roleId=0)和管理员(roleId=1)可操作
     * 操作日志：删除前记录操作人和删除内容
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<Integer> ids, Integer operatorId, Integer operatorRole) {
        // 权限校验：普通用户(roleId=2)无权批量删除
        if (operatorRole != null && operatorRole == 2) {
            throw new RuntimeException("无权限执行批量删除操作");
        }

        if (ids == null || ids.isEmpty()) {
            return 0;
        }

        // 删除前记录操作日志
        List<Record> recordsToDelete = recordMapper.selectBatchIds(ids);
        for (Record r : recordsToDelete) {
            log.info("[批量删除流转记录] 操作人ID={}, 删除记录: id={}, goods={}, count={}, action={}",
                    operatorId, r.getId(), r.getGoods(), r.getCount(), r.getRemark());
        }

        return recordMapper.deleteBatchIds(ids);
    }

    /**
     * 预扣减库存
     * 出库前先预占库存，创建status=0的预扣减记录
     * 库存数量立即减少，但记录标记为"未确认"
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer preDeduct(Record record) {
        Goods goods = goodsService.getById(record.getGoods());
        if (goods == null) {
            throw new RuntimeException("药品不存在");
        }

        int n = record.getCount();
        if ("2".equals(record.getAction())) {
            n = -n;
            record.setCount(n);
        }

        int newCount = goods.getCount() + n;
        if (newCount < 0) {
            throw new RuntimeException("库存不足，当前库存：" + goods.getCount() + "，尝试出库：" + (-n));
        }

        goods.setCount(newCount);
        boolean updateSuccess = goodsService.updateById(goods);
        if (!updateSuccess) {
            throw new RuntimeException("并发操作冲突，请重试");
        }

        // 标记为预扣减状态
        record.setStatus(0);
        recordMapper.insert(record);
        return record.getId();
    }

    /**
     * 确认预扣减
     * 将预扣减记录状态改为已确认
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmDeduct(Integer recordId) {
        Record record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("预扣减记录不存在");
        }
        if (record.getStatus() != null && record.getStatus() == 1) {
            throw new RuntimeException("该记录已确认，无需重复操作");
        }
        record.setStatus(1);
        return recordMapper.updateById(record) > 0;
    }

    /**
     * 回滚预扣减
     * 超时未确认时，恢复库存并删除预扣减记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean rollbackDeduct(Integer recordId) {
        Record record = recordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("预扣减记录不存在");
        }
        if (record.getStatus() != null && record.getStatus() == 1) {
            throw new RuntimeException("该记录已确认，无法回滚");
        }

        // 恢复库存
        Goods goods = goodsService.getById(record.getGoods());
        if (goods != null) {
            int restoreCount = -record.getCount(); // 取负值恢复
            goods.setCount(goods.getCount() + restoreCount);
            boolean updateSuccess = goodsService.updateById(goods);
            if (!updateSuccess) {
                throw new RuntimeException("回滚库存时并发冲突，请重试");
            }
        }

        // 删除预扣减记录
        return recordMapper.deleteById(recordId) > 0;
    }
}
