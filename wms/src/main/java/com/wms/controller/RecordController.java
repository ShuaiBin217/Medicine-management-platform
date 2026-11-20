package com.wms.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.AuthUtils;
import com.wms.common.CacheConstant;
import com.wms.common.IdSequence;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.entity.Record;
import com.wms.service.GoodsService;
import com.wms.service.RedisService;
import com.wms.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/record")
public class RecordController {

    @Autowired
    private RecordService recordService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private IdSequence idSequence;

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String)param.get("name");
        String goodstype = (String)param.get("goodstype");
        String storage = (String)param.get("storage");
        Object statusParam = param.get("status");
        String status = statusParam == null ? null : String.valueOf(statusParam);
        String roleId = (String)param.get("roleId");
        String userId = (String)param.get("userId");

        Page<Record> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        QueryWrapper<Record> queryWrapper = new QueryWrapper();
        queryWrapper.apply(" a.goods=b.id and b.storage=c.id and b.goodsType=d.id ");

        if("2".equals(roleId)){
            queryWrapper.apply(" a.userId= "+userId);
        }

        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
            queryWrapper.like("b.name",name);
        }
        if(StringUtils.isNotBlank(goodstype) && !"null".equals(goodstype)){
            queryWrapper.eq("d.id",goodstype);
        }
        if(StringUtils.isNotBlank(storage) && !"null".equals(storage)){
            queryWrapper.eq("c.id",storage);
        }
        // 状态筛选：0 待确认（预扣减中），1 已确认，2 已回滚
        if(StringUtils.isNotBlank(status) && !"null".equals(status)){
            queryWrapper.eq("a.status",status);
        }

        IPage result = recordService.pageCC(page,queryWrapper);
        return Result.suc(result.getRecords(),result.getTotal());
    }

    /**
     * 事务性保存流转记录（含库存同步更新+乐观锁防超卖）
     * 入库直接生效；出库请走 /preDeduct，确认后才生效
     */
    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result save(@RequestBody Record record){
        try {
            boolean success = recordService.saveWithInventory(record);
            if (success) {
                clearGoodsCache();
                return Result.suc();
            }
            return Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 批量删除流转记录（含权限校验+操作日志）
     * 操作人取自登录态，不再信任请求体里的 operatorId / operatorRole
     */
    @PostMapping("/batchDelete")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result batchDelete(@RequestBody Map<String, Object> body) {
        try {
            @SuppressWarnings("unchecked")
            List<Integer> ids = (List<Integer>) body.get("ids");
            Integer operatorId = AuthUtils.currentUserId();
            Integer operatorRole = AuthUtils.currentRole();
            if (operatorId == null) {
                operatorId = (Integer) body.get("operatorId");
            }

            if (ids == null || ids.isEmpty()) {
                return Result.fail("请选择要删除的记录");
            }

            int deleted = recordService.batchDelete(ids, operatorId, operatorRole);
            if (deleted > 0) {
                idSequence.reset("record");
            }
            Map<String, Object> data = new HashMap<>();
            data.put("deleted", deleted);
            return Result.suc(data);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 预扣减库存（出库前预占库存，生成 status=0 的待确认记录）
     */
    @PostMapping("/preDeduct")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result preDeduct(@RequestBody Record record) {
        try {
            Integer recordId = recordService.preDeduct(record);
            clearGoodsCache();
            return Result.suc(recordId);
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 确认预扣减（仅待确认记录可确认，库存不再变动）
     */
    @PostMapping("/confirmDeduct")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result confirmDeduct(@RequestParam Integer id) {
        return doConfirmDeduct(id);
    }

    /**
     * 回滚预扣减（恢复库存，记录标记为已回滚并保留）
     */
    @PostMapping("/rollbackDeduct")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result rollbackDeduct(@RequestParam Integer id) {
        return doRollbackDeduct(id);
    }

    /**
     * 兼容旧测试脚本：GET 方式确认预扣减（建议改用 POST）
     */
    @Deprecated
    @GetMapping("/confirmDeduct")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result confirmDeductByGet(@RequestParam Integer id) {
        return doConfirmDeduct(id);
    }

    /**
     * 兼容旧测试脚本：GET 方式回滚预扣减（建议改用 POST）
     */
    @Deprecated
    @GetMapping("/rollbackDeduct")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result rollbackDeductByGet(@RequestParam Integer id) {
        return doRollbackDeduct(id);
    }

    private Result doConfirmDeduct(Integer id) {
        try {
            boolean success = recordService.confirmDeduct(id);
            return success ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    private Result doRollbackDeduct(Integer id) {
        try {
            boolean success = recordService.rollbackDeduct(id);
            if (success) {
                clearGoodsCache();
            }
            return success ? Result.suc() : Result.fail();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        }
    }

    private void clearGoodsCache() {
        redisService.delete(CacheConstant.GOODS_LIST);
        redisService.delete(CacheConstant.STOCK_SUMMARY);
        redisService.deleteByPattern(CacheConstant.GOODS_NAME_PREFIX + "*");
        redisService.deleteByPattern(CacheConstant.GOODS_TYPE_PREFIX + "*");
        redisService.deleteByPattern(CacheConstant.GOODS_STORAGE_PREFIX + "*");
        redisService.deleteByPattern(CacheConstant.GOODS_LOW_STOCK_PREFIX + "*");
    }
}
