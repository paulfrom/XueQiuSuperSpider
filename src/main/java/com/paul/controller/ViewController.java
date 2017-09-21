package com.paul.controller;

import com.google.common.collect.Lists;
import com.paul.mapper.StockInfoMapper;
import com.paul.utils.RedisManager;
import org.decaywood.entity.Stock;
import org.decaywood.entity.StockInfo;
import org.decaywood.mapper.stockFirst.StockInitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.rmi.RemoteException;
import java.util.List;

/**
 * Created by liusonglin
 * Date:2017/9/20
 * Description:
 */
@RestController
public class ViewController {

    @Autowired
    private RedisManager redisManager;

    @Autowired
    StockInitMapper stockInitMapper;

    @GetMapping("/index")
    public StockInfo stringList(){
        return redisManager.getFromMap("stock","SH601993",StockInfo.class);
    }


    @GetMapping("/initStock")
    public List<StockInfo> initStock(){

        try {
            List<StockInfo> lists = stockInitMapper.mapLogic(null);

            return lists;
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
