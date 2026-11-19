package com.wms.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 药品视图对象：在 {@link Goods} 基础上带出药房名、分类名，
 * 供"药品流转"页展示，避免前端再拿 id 反查。
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class GoodsView extends Goods {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "药房名称")
    private String storagename;

    @ApiModelProperty(value = "分类名称")
    private String goodstypename;
}
