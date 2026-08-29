package com.wms.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.CacheConstant;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Goodstype;
import com.wms.service.GoodstypeService;
import com.wms.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/goodstype")
public class GoodstypeController {
    @Autowired
    private GoodstypeService goodstypeService;

    @Autowired
    private RedisService redisService;

    @PostMapping("/save")
    public Result save(@RequestBody Goodstype goodstype){
        boolean result = goodstypeService.save(goodstype);
        if (result) {
            clearCategoryCache();
        }
        return result ? Result.suc() : Result.fail();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Goodstype goodstype){
        boolean result = goodstypeService.updateById(goodstype);
        if (result) {
            clearCategoryCache();
        }
        return result ? Result.suc() : Result.fail();
    }

    @GetMapping("/del")
    public Result del(@RequestParam String id){
        boolean result = goodstypeService.removeById(id);
        if (result) {
            clearCategoryCache();
        }
        return result ? Result.suc() : Result.fail();
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String)param.get("name");

        Page<Goodstype> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<Goodstype> lambdaQueryWrapper = new LambdaQueryWrapper();
        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
            lambdaQueryWrapper.like(Goodstype::getName,name);
        }

        IPage result = goodstypeService.pageCC(page,lambdaQueryWrapper);
        return Result.suc(result.getRecords(),result.getTotal());
    }

    @GetMapping("/list")
    public Result list(){
        List list = goodstypeService.list();
        return Result.suc(list);
    }

    private void clearCategoryCache() {
        redisService.delete(CacheConstant.CATEGORY_LIST);
        redisService.delete(CacheConstant.GOODS_LIST);
        redisService.deleteByPattern(CacheConstant.GOODS_TYPE_PREFIX + "*");
    }
}