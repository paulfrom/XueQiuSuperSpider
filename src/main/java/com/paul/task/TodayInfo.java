package com.paul.task;

import com.alibaba.fastjson.JSON;
import com.paul.mapper.StockInfoMapper;
import com.paul.mapper.StockMapper;
import com.paul.utils.RedisManager;
import lombok.extern.slf4j.Slf4j;
import org.decaywood.entity.Stock;
import org.decaywood.entity.StockInfo;
import org.decaywood.mapper.stockFirst.StockToStockWithAttributeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

/**
 * Created by liusonglin
 * Date:2017/9/21
 * Description:
 */
@Component
@Slf4j
public class TodayInfo {

    @Autowired
    private StockInfoMapper stockInfoMapper;

    @Autowired
    private StockMapper stockMapper;


    @Scheduled(cron="0 44 17 * * ?")
    public void run(){
        try {
            StockToStockWithAttributeMapper attributeMapper = new StockToStockWithAttributeMapper();
            List<StockInfo> stockInfoList = stockInfoMapper.selectAll();
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
            e.printStackTrace();
        }
    }

}