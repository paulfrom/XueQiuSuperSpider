package com.paul.task;

import com.alibaba.fastjson.JSON;
import com.paul.mapper.StockInfoMapper;
import com.paul.mapper.StockMapper;
import lombok.extern.slf4j.Slf4j;
import org.decaywood.entity.Stock;
import org.decaywood.entity.StockInfo;
import org.decaywood.mapper.stockFirst.StockToStockWithAttributeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by liusonglin
 * Date:2017/9/21
 * Description:
 */
@Component
@Slf4j
public class ExtensionInfo {

    @Autowired
    private StockInfoMapper stockInfoMapper;

    @Autowired
    private StockMapper stockMapper;


    public void run(){
        log.info("start extension info task");
        try {
            StockToStockWithAttributeMapper attributeMapper = new StockToStockWithAttributeMapper();
            Example example = new Example(StockInfo.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andCondition("`code` not in (select stock_no from stockDetail where stock_Query_Date = (select max(stock_query_date) from stockDetail))");
            List<StockInfo> stockInfoList = stockInfoMapper.selectByExample(example);
            stockInfoList.stream().forEach(item->{
                Stock stock = new Stock(item.getName(),item.getCode());
                try {
                    Stock info = attributeMapper.mapLogic(stock);
                    log.info("item is : {}", JSON.toJSONString(info));
                    stockMapper.insert(info);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (RemoteException e) {
            log.error("remote exception {}",e);
        }

        log.info("end extension info task");
    }

}
