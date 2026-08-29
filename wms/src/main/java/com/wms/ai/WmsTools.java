package com.wms.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.CacheConstant;
import com.wms.entity.Goods;
import com.wms.entity.Goodstype;
import com.wms.entity.Storage;
import com.wms.service.GoodsService;
import com.wms.service.GoodstypeService;
import com.wms.service.RedisService;
import com.wms.service.StorageService;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class WmsTools {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private GoodstypeService goodstypeService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Tool("查询所有药品的库存信息，包括药品名、分类、药房、库存数量和备注")
    public String queryAllGoods() {
        String cached = (String) redisService.get(CacheConstant.GOODS_LIST);
        if (cached != null) {
            return cached;
        }

        List<Goods> goodsList = goodsService.list();
        Map<Integer, String> typeMap = getTypeMap();
        Map<Integer, String> storageMap = getStorageMap();

        StringBuilder sb = new StringBuilder();
        sb.append("共 ").append(goodsList.size()).append(" 种药品：\n");
        for (Goods g : goodsList) {
            sb.append("- ").append(g.getName())
                    .append(" | 分类: ").append(typeMap.getOrDefault(g.getGoodstype(), "未知"))
                    .append(" | 药房: ").append(storageMap.getOrDefault(g.getStorage(), "未知"))
                    .append(" | 库存: ").append(g.getCount())
                    .append(" | 备注: ").append(g.getRemark() != null ? g.getRemark() : "无")
                    .append("\n");
        }

        String result = sb.toString();
        redisService.set(CacheConstant.GOODS_LIST, result, CacheConstant.TTL_GOODS_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    @Tool("根据药品名称模糊查询药品库存信息")
    public String queryGoodsByName(String name) {
        String cacheKey = CacheConstant.GOODS_NAME_PREFIX + name;
        String cached = (String) redisService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Goods::getName, name);
        List<Goods> goodsList = goodsService.list(wrapper);
        if (goodsList.isEmpty()) {
            return "未找到名称包含「" + name + "」的药品";
        }
        Map<Integer, String> typeMap = getTypeMap();
        Map<Integer, String> storageMap = getStorageMap();

        StringBuilder sb = new StringBuilder();
        sb.append("找到 ").append(goodsList.size()).append(" 种药品：\n");
        for (Goods g : goodsList) {
            sb.append("- ").append(g.getName())
                    .append(" | 分类: ").append(typeMap.getOrDefault(g.getGoodstype(), "未知"))
                    .append(" | 药房: ").append(storageMap.getOrDefault(g.getStorage(), "未知"))
                    .append(" | 库存: ").append(g.getCount())
                    .append(" | 备注: ").append(g.getRemark() != null ? g.getRemark() : "无")
                    .append("\n");
        }

        String result = sb.toString();
        redisService.set(cacheKey, result, CacheConstant.TTL_TOOL_QUERY_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    @Tool("查询库存数量低于或等于指定阈值的药品，用于低库存预警")
    public String queryLowStockGoods(int threshold) {
        String cacheKey = CacheConstant.GOODS_LOW_STOCK_PREFIX + threshold;
        String cached = (String) redisService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Goods::getCount, threshold);
        List<Goods> goodsList = goodsService.list(wrapper);
        if (goodsList.isEmpty()) {
            return "当前没有库存低于 " + threshold + " 的药品，库存充足！";
        }
        Map<Integer, String> typeMap = getTypeMap();
        Map<Integer, String> storageMap = getStorageMap();

        StringBuilder sb = new StringBuilder();
        sb.append("以下 ").append(goodsList.size()).append(" 种药品库存低于等于 ").append(threshold).append("，需要补货：\n");
        for (Goods g : goodsList) {
            sb.append("- ").append(g.getName())
                    .append(" | 分类: ").append(typeMap.getOrDefault(g.getGoodstype(), "未知"))
                    .append(" | 药房: ").append(storageMap.getOrDefault(g.getStorage(), "未知"))
                    .append(" | 当前库存: ").append(g.getCount())
                    .append("\n");
        }

        String result = sb.toString();
        redisService.set(cacheKey, result, CacheConstant.TTL_TOOL_QUERY_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    @Tool("查询指定分类下的所有药品")
    public String queryGoodsByTypeName(String typeName) {
        String cacheKey = CacheConstant.GOODS_TYPE_PREFIX + typeName;
        String cached = (String) redisService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Goodstype type = goodstypeService.lambdaQuery()
                .eq(Goodstype::getName, typeName).one();
        if (type == null) {
            return "未找到分类「" + typeName + "」，请检查分类名称";
        }
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getGoodstype, type.getId());
        List<Goods> goodsList = goodsService.list(wrapper);
        if (goodsList.isEmpty()) {
            return "分类「" + typeName + "」下暂无药品";
        }
        Map<Integer, String> storageMap = getStorageMap();

        StringBuilder sb = new StringBuilder();
        sb.append("分类「").append(typeName).append("」下共 ").append(goodsList.size()).append(" 种药品：\n");
        for (Goods g : goodsList) {
            sb.append("- ").append(g.getName())
                    .append(" | 药房: ").append(storageMap.getOrDefault(g.getStorage(), "未知"))
                    .append(" | 库存: ").append(g.getCount())
                    .append(" | 备注: ").append(g.getRemark() != null ? g.getRemark() : "无")
                    .append("\n");
        }

        String result = sb.toString();
        redisService.set(cacheKey, result, CacheConstant.TTL_TOOL_QUERY_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    @Tool("查询指定药房下的所有药品")
    public String queryGoodsByStorageName(String storageName) {
        String cacheKey = CacheConstant.GOODS_STORAGE_PREFIX + storageName;
        String cached = (String) redisService.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        Storage storage = storageService.lambdaQuery()
                .eq(Storage::getName, storageName).one();
        if (storage == null) {
            return "未找到药房「" + storageName + "」，请检查药房名称";
        }
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStorage, storage.getId());
        List<Goods> goodsList = goodsService.list(wrapper);
        if (goodsList.isEmpty()) {
            return "药房「" + storageName + "」下暂无药品";
        }
        Map<Integer, String> typeMap = getTypeMap();

        StringBuilder sb = new StringBuilder();
        sb.append("药房「").append(storageName).append("」下共 ").append(goodsList.size()).append(" 种药品：\n");
        for (Goods g : goodsList) {
            sb.append("- ").append(g.getName())
                    .append(" | 分类: ").append(typeMap.getOrDefault(g.getGoodstype(), "未知"))
                    .append(" | 库存: ").append(g.getCount())
                    .append(" | 备注: ").append(g.getRemark() != null ? g.getRemark() : "无")
                    .append("\n");
        }

        String result = sb.toString();
        redisService.set(cacheKey, result, CacheConstant.TTL_TOOL_QUERY_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    @Tool("查询所有药品分类列表")
    public String queryAllCategories() {
        String cached = (String) redisService.get(CacheConstant.CATEGORY_LIST);
        if (cached != null) {
            return cached;
        }

        List<Goodstype> typeList = goodstypeService.list();
        if (typeList.isEmpty()) {
            return "暂无药品分类数据";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("共 ").append(typeList.size()).append(" 个分类：\n");
        for (Goodstype t : typeList) {
            long count = goodsService.lambdaQuery().eq(Goods::getGoodstype, t.getId()).count();
            sb.append("- ").append(t.getName())
                    .append("（含 ").append(count).append(" 种药品）")
                    .append(t.getRemark() != null ? " | 备注: " + t.getRemark() : "")
                    .append("\n");
        }

        String result = sb.toString();
        redisService.set(CacheConstant.CATEGORY_LIST, result, CacheConstant.TTL_CATEGORY_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    @Tool("查询所有药房/仓库列表")
    public String queryAllStorages() {
        String cached = (String) redisService.get(CacheConstant.STORAGE_LIST);
        if (cached != null) {
            return cached;
        }

        List<Storage> storageList = storageService.list();
        if (storageList.isEmpty()) {
            return "暂无药房数据";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("共 ").append(storageList.size()).append(" 个药房：\n");
        for (Storage s : storageList) {
            long count = goodsService.lambdaQuery().eq(Goods::getStorage, s.getId()).count();
            sb.append("- ").append(s.getName())
                    .append("（含 ").append(count).append(" 种药品）")
                    .append(s.getRemark() != null ? " | 备注: " + s.getRemark() : "")
                    .append("\n");
        }

        String result = sb.toString();
        redisService.set(CacheConstant.STORAGE_LIST, result, CacheConstant.TTL_STORAGE_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    @Tool("查询库存统计摘要：总药品数、总库存量、低库存预警数")
    public String queryStockSummary() {
        String cached = (String) redisService.get(CacheConstant.STOCK_SUMMARY);
        if (cached != null) {
            return cached;
        }

        List<Goods> goodsList = goodsService.list();
        int totalKinds = goodsList.size();
        int totalStock = goodsList.stream().mapToInt(Goods::getCount).sum();
        long lowStockCount = goodsList.stream().filter(g -> g.getCount() <= 10).count();
        long zeroStockCount = goodsList.stream().filter(g -> g.getCount() <= 0).count();

        StringBuilder sb = new StringBuilder();
        sb.append("库存统计摘要：\n");
        sb.append("- 药品种类总数: ").append(totalKinds).append("\n");
        sb.append("- 库存总量: ").append(totalStock).append("\n");
        sb.append("- 低库存预警(≤10): ").append(lowStockCount).append(" 种\n");
        sb.append("- 缺货(≤0): ").append(zeroStockCount).append(" 种\n");

        String result = sb.toString();
        redisService.set(CacheConstant.STOCK_SUMMARY, result, CacheConstant.TTL_SUMMARY_MINUTES, TimeUnit.MINUTES);
        return result;
    }

    @Tool("从药品知识库中检索药品说明书、用药指南、用法用量、禁忌症、不良反应等专业文档内容")
    public String searchKnowledgeBase(String query) {
        return knowledgeBaseService.search(query);
    }

    private Map<Integer, String> getTypeMap() {
        return goodstypeService.list().stream()
                .collect(Collectors.toMap(Goodstype::getId, Goodstype::getName));
    }

    private Map<Integer, String> getStorageMap() {
        return storageService.list().stream()
                .collect(Collectors.toMap(Storage::getId, Storage::getName));
    }
}