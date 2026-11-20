package com.wms.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.AuthUtils;
import com.wms.common.CacheConstant;
import com.wms.common.IdSequence;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.entity.GoodsView;
import com.wms.entity.Record;
import com.wms.entity.RecordRes;
import com.wms.service.GoodsService;
import com.wms.service.RecordService;
import com.wms.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goods")
public class GoodsController {

   @Autowired
    private GoodsService goodsService;

   @Autowired
   private RecordService recordService;

   @Autowired
   private IdSequence idSequence;

   @Autowired
   private RedisService redisService;

    @PostMapping("/save")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result save(@RequestBody Goods goods){
        boolean result = goodsService.save(goods);
        if (result) {
            clearGoodsCache();
        }
        return result ? Result.suc() : Result.fail();
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result update(@RequestBody Goods goods){
        if (goods.getId() == null) {
            return Result.fail("参数错误");
        }
        Goods existing = goodsService.getById(goods.getId());
        if (existing == null) {
            return Result.fail("药品不存在");
        }
        // 库存只能通过采购/发药变更，编辑只维护药品档案，防止绕过流水直接改数量
        goods.setCount(existing.getCount());
        boolean result = goodsService.updateById(goods);
        if (result) {
            clearGoodsCache();
        }
        return result ? Result.suc() : Result.fail();
    }

    @GetMapping("/del")
    @PreAuthorize("hasAnyRole('0','1')")
    public Result del(@RequestParam String id){
        Integer goodsId;
        try {
            goodsId = Integer.valueOf(id);
        } catch (NumberFormatException e) {
            return Result.fail("参数错误");
        }
        // 有历史流水的药品不回收自增 id：否则新药品占用同一 id 时，
        // 旧流水会显示到新药品名下（记录表的 goods 字段仍指向该 id）
        boolean hasFlow = recordService.lambdaQuery()
                .eq(Record::getGoods, goodsId)
                .count() > 0;
        boolean result = goodsService.removeById(goodsId);
        if (result) {
            if (!hasFlow) {
                idSequence.reset("goods");
            }
            clearGoodsCache();
        }
        return result ? Result.suc() : Result.fail();
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String)param.get("name");
        String goodstype = (String)param.get("goodstype");
        String storage = (String)param.get("storage");

        Page<Goods> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<Goods> lambdaQueryWrapper = new LambdaQueryWrapper();
        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
            lambdaQueryWrapper.like(Goods::getName,name);
        }
        if(StringUtils.isNotBlank(goodstype) && !"null".equals(goodstype)){
            lambdaQueryWrapper.eq(Goods::getGoodstype,goodstype);
        }
        if(StringUtils.isNotBlank(storage) && !"null".equals(storage)){
            lambdaQueryWrapper.eq(Goods::getStorage,storage);
        }

        IPage result = goodsService.pageCC(page,lambdaQueryWrapper);
        return Result.suc(result.getRecords(),result.getTotal());
    }

    /**
     * 药品流转详情：药品信息 + 同名药品在各药房的分布 + 出入库流水 + 汇总
     *
     * <p>一次请求返回整页所需数据，避免前端串行请求。普通用户（roleId=2）
     * 只能看到自己经手的流水，与记录管理页的可见范围保持一致。</p>
     */
    @GetMapping("/detail")
    public Result detail(@RequestParam Integer id){
        GoodsView goods = goodsService.detailView(id);
        if (goods == null) {
            return Result.fail("药品不存在");
        }

        List<GoodsView> sameName = goodsService.listSameName(goods.getName());

        Integer userId = null;
        if (Integer.valueOf(2).equals(AuthUtils.currentRole())) {
            userId = AuthUtils.currentUserId();
            if (userId == null) {
                // 旧会话里取不到当前用户 id 时按"无权限看别人的流水"处理
                userId = -1;
            }
        }
        List<RecordRes> records = recordService.listFlow(goods.getName(), null, userId);

        int totalIn = 0;
        int totalOut = 0;
        int pending = 0;
        for (RecordRes record : records) {
            // 已回滚的单据不再计入累计出入库，避免取消掉的出库被统计
            if (record.getStatus() != null && record.getStatus() == 2) {
                continue;
            }
            int count = record.getCount() == null ? 0 : record.getCount();
            if (count >= 0) {
                totalIn += count;
            } else {
                totalOut += -count;
            }
            if (record.getStatus() != null && record.getStatus() == 0) {
                pending++;
            }
        }

        int totalStock = 0;
        for (GoodsView item : sameName) {
            totalStock += item.getCount() == null ? 0 : item.getCount();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("goods", goods);
        data.put("sameName", sameName);
        data.put("records", records);
        data.put("totalIn", totalIn);
        data.put("totalOut", totalOut);
        data.put("pending", pending);
        data.put("totalStock", totalStock);
        return Result.suc(data);
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
