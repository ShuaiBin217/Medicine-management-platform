package com.wms.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.CacheConstant;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goods;
import com.wms.service.GoodsService;
import com.wms.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/goods")
public class GoodsController {

   @Autowired
    private GoodsService goodsService;

   @Autowired
   private RedisService redisService;

    @PostMapping("/save")
    public Result save(@RequestBody Goods goods){
        boolean result = goodsService.save(goods);
        if (result) {
            clearGoodsCache();
        }
        return result ? Result.suc() : Result.fail();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Goods goods){
        boolean result = goodsService.updateById(goods);
        if (result) {
            clearGoodsCache();
        }
        return result ? Result.suc() : Result.fail();
    }

    @GetMapping("/del")
    public Result del(@RequestParam String id){
        boolean result = goodsService.removeById(id);
        if (result) {
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

    private void clearGoodsCache() {
        redisService.delete(CacheConstant.GOODS_LIST);
        redisService.delete(CacheConstant.STOCK_SUMMARY);
        redisService.deleteByPattern(CacheConstant.GOODS_NAME_PREFIX + "*");
        redisService.deleteByPattern(CacheConstant.GOODS_TYPE_PREFIX + "*");
        redisService.deleteByPattern(CacheConstant.GOODS_STORAGE_PREFIX + "*");
        redisService.deleteByPattern(CacheConstant.GOODS_LOW_STOCK_PREFIX + "*");
    }
}