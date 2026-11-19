package com.wms.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wms.entity.Goods;
import com.wms.entity.GoodsView;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wms.entity.Storage;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wms
 * 
 */
public interface GoodsService extends IService<Goods> {
    IPage pageCC(IPage<Goods> page, Wrapper wrapper);

    /**
     * 查询单个药品详情（带药房名、分类名）
     */
    GoodsView detailView(Integer id);

    /**
     * 查询同名药品在各药房的分布
     */
    List<GoodsView> listSameName(String name);
}
