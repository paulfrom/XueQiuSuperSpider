package com.paul.entity;

import lombok.Data;

/**
 * Created by liusonglin
 * Date:2017/9/22
 * Description:
 */
@Data
public class MyGuideline {

    public String stockName;

    public String stockCode;

    //上影线和实体比
    public Double upperShadowEntityRate;

    //实体和下影线对比
    public Double downShadowEntityRate;

    //是否上涨
    public boolean isUpper;

    //动态市盈率
    public String pe_ttm;
}
