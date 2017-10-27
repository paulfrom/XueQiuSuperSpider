package com.paul.mapper;

import com.paul.entity.CountLine;
import com.paul.utils.MyMapper;
import org.decaywood.entity.Stock;

import java.util.List;
import java.util.Map;

/**
 * Created by liusonglin
 * Date:2017/9/20
 * Description:
 */
public interface StockMapper extends MyMapper<Stock> {
    List<CountLine> countByDate();
}
