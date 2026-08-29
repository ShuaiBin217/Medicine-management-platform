package com.wms.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.CacheConstant;
import com.wms.common.QueryPageParam;
import com.wms.common.Result;
import com.wms.entity.Storage;
import com.wms.service.RedisService;
import com.wms.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/storage")
public class StorageController {

    @Autowired
    private StorageService storageService;

    @Autowired
    private RedisService redisService;

    @PostMapping("/save")
    public Result save(@RequestBody Storage storage){
        boolean result = storageService.save(storage);
        if (result) {
            clearStorageCache();
        }
        return result ? Result.suc() : Result.fail();
    }

    @PostMapping("/update")
    public Result update(@RequestBody Storage storage){
        boolean result = storageService.updateById(storage);
        if (result) {
            clearStorageCache();
        }
        return result ? Result.suc() : Result.fail();
    }

    @GetMapping("/del")
    public Result del(@RequestParam String id){
        boolean result = storageService.removeById(id);
        if (result) {
            clearStorageCache();
        }
        return result ? Result.suc() : Result.fail();
    }

    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query){
        HashMap param = query.getParam();
        String name = (String)param.get("name");

        Page<Storage> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        LambdaQueryWrapper<Storage> lambdaQueryWrapper = new LambdaQueryWrapper();
        if(StringUtils.isNotBlank(name) && !"null".equals(name)){
            lambdaQueryWrapper.like(Storage::getName,name);
        }

        IPage result = storageService.pageCC(page,lambdaQueryWrapper);
        return Result.suc(result.getRecords(),result.getTotal());
    }

    @GetMapping("/list")
    public Result list(){
        List list = storageService.list();
        return Result.suc(list);
    }

    private void clearStorageCache() {
        redisService.delete(CacheConstant.STORAGE_LIST);
        redisService.delete(CacheConstant.GOODS_LIST);
        redisService.deleteByPattern(CacheConstant.GOODS_STORAGE_PREFIX + "*");
    }
}