package com.paul.controller;

import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.paul.entity.CountLine;
import com.paul.entity.MyGuideline;
import com.paul.mapper.StockInfoMapper;
import com.paul.mapper.StockMapper;
import com.paul.utils.RedisManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.decaywood.entity.Entry;
import org.decaywood.entity.Stock;
import org.decaywood.entity.StockInfo;
import org.decaywood.entity.trend.StockTrend;
import org.decaywood.mapper.stockFirst.StockInitMapper;
import org.decaywood.mapper.stockFirst.StockToStockWithStockTrendMapper;
import org.decaywood.utils.MathUtils;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import tk.mybatis.mapper.entity.Example;

import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by liusonglin
 * Date:2017/9/20
 * Description:
 */
@RestController
@Slf4j
public class ViewController {

    @Autowired
    private RedisManager redisManager;

    @Autowired
    StockInitMapper stockInitMapper;

    @Autowired
    StockInfoMapper stockInfoMapper;

    @Autowired
    StockMapper stockMapper;

    @Autowired
    MyBatisCursorItemReader<StockInfo> myBatisCursorItemReader;

    @Autowired
    MyBatisPagingItemReader<StockInfo> myBatisPagingItemReader;

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


    @GetMapping("/one")
    public List<String> oneStrategy() throws RemoteException {
        Example example = new Example(Stock.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andCondition("stock_Query_Date = (select max(stock_query_date) from stockDetail)");
        List<Stock> list = stockMapper.selectByExample(example);
        StockToStockWithStockTrendMapper mapper = new StockToStockWithStockTrendMapper();

        Predicate<Entry<String, Stock>> predicate = x -> {

            if(x.getValue().getStockTrend().getHistory().isEmpty()) return false;
            List<StockTrend.TrendBlock> history = x.getValue().getStockTrend().getHistory();
            StockTrend.TrendBlock block = history.get(history.size() - 1);
            double close = Double.parseDouble(block.getClose());
            double open = Double.parseDouble(block.getOpen());
            double ma5 = Double.parseDouble(block.getMa5());
            double ma10 = Double.parseDouble(block.getMa10());
            double ma30 = Double.parseDouble(block.getMa30());

            double max = Math.max(close, open);
            double min = Math.min(close, open);

            return close > open && max >= MathUtils.max(ma5, ma10, ma30) && min <= MathUtils.min(ma5, ma10, ma30);

        };
        return list.parallelStream()
                .map(item -> new Entry<>(item.getStockName(),mapper.apply(item)))
                .filter(predicate)
                .map(Entry::getKey)
                .collect(Collectors.toList());
    }


    @GetMapping("/two/{multiple}")
    public List<MyGuideline> twoStrategy(@PathVariable("multiple") Integer multiple){
        Example example = new Example(Stock.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andCondition("stock_Query_Date = (select max(stock_query_date) from stockDetail)");
        List<Stock> list = stockMapper.selectByExample(example);

        Function<Stock,MyGuideline> function = item -> {
            double close = Double.parseDouble(item.getClose());
            double open = Double.parseDouble(item.getOpenAmt());
            double hightest = Double.parseDouble(item.getHigh());
            double lowest = Double.parseDouble(item.getLow());

            double max = Math.max(close, open);
            double min = Math.min(close, open);

            double urate;

            double drate;

            if(max == min){
                urate = 0;
                drate = 0;
            }else {
                urate = (hightest-max)/(max-min);
                if(min == lowest){
                    drate = 0;
                }else {
                    drate = (max-min)/(min-lowest);
                }
            }

            MyGuideline myGuideline = new MyGuideline();
            myGuideline.setStockCode(item.getStockNo());
            myGuideline.setStockName(item.getStockName());
            myGuideline.setUpperShadowEntityRate(urate);
            myGuideline.setUpper(max==close);
            myGuideline.setPe_ttm(item.getPe_ttm());
            myGuideline.setDownShadowEntityRate(drate);
            return myGuideline;
        };

        return list.parallelStream()
                .map(function)
                .filter(item -> item.getDownShadowEntityRate()==0 && item.getUpperShadowEntityRate() > multiple)
                .sorted((s1,s2)->s2.getUpperShadowEntityRate().compareTo(s1.getUpperShadowEntityRate()))
                .collect(Collectors.toList());
    }


    @GetMapping("/uprate")
    public List<StockInfo> rateCompare(){
        List<StockInfo> stockInfoList = stockInfoMapper.selectAll();

        stockInfoList.parallelStream().forEach(item ->{

            PageHelper.offsetPage(0,5);
            Example example = new Example(Stock.class);
            example.orderBy("stockQueryDate").desc();
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("stockNo",item.getCode());
            List<Stock> stockList = stockMapper.selectByExample(example);
            if(stockList.size()<2)return;
            stockList = stockList.parallelStream()
                    .sorted(Comparator.comparing((s) ->
                            new BigDecimal(StringUtils.isNotBlank(s.getFloat_market_capital())?s.getFloat_market_capital():"0")))
                    .collect(Collectors.toList());
            BigDecimal minCapital,maxCapital;
            String maxCapitalStr = stockList
                    .parallelStream()
                    .max(Comparator.comparing((s) -> new BigDecimal(StringUtils.isNotBlank(s.getFloat_market_capital())?s.getFloat_market_capital():"0")))
                    .get()
                    .getFloat_market_capital();
            maxCapital = new BigDecimal(StringUtils.isNotBlank(maxCapitalStr)?maxCapitalStr:"0");
            String minCapitalStr = stockList
                    .parallelStream()
                    .min(Comparator.comparing((s) -> new BigDecimal(StringUtils.isNotBlank(s.getFloat_market_capital())?s.getFloat_market_capital():"0")))
                    .get()
                    .getFloat_market_capital();
            minCapital = new BigDecimal(StringUtils.isNotBlank(minCapitalStr)?minCapitalStr:"0");

            BigDecimal minCLose,maxClose;
            String minCLoseStr = stockList
                    .parallelStream()
                    .min(Comparator.comparing((s) -> new BigDecimal(s.getClose())))
                    .get()
                    .getClose();
            minCLose = new BigDecimal(StringUtils.isNotBlank(minCLoseStr)?minCLoseStr:"0");
            String maxCloseStr = stockList
                    .parallelStream()
                    .max(Comparator.comparing((s) -> new BigDecimal(s.getClose())))
                    .get()
                    .getClose();
            maxClose = new BigDecimal(StringUtils.isNotBlank(maxCloseStr)?maxCloseStr:"0");

            double capitalRate = maxCapital.subtract(minCapital).doubleValue()/maxCapital.doubleValue();

            double closeRate = maxClose.subtract(minCLose).doubleValue()/maxClose.doubleValue();






            log.info("capitalRate ==== {} , closeRate ==== {} ",capitalRate,closeRate);


        });

        return null;
    }

    @GetMapping("/count")
    public List<CountLine> count(){
        return stockMapper.countByDate();
    }


    @GetMapping("/test")
    public Integer test() throws Exception {
        try{
//            Map<String,Object> para = new HashMap<>(1);
//            para.put("hasexist","true");
//            myBatisCursorItemReader.setParameterValues(para);
            myBatisCursorItemReader.open(new ExecutionContext());
            List<StockInfo> stockInfoList = new ArrayList(10);
            StockInfo stockInfo;
            while ((stockInfo=myBatisCursorItemReader.read())!=null) {
                stockInfoList.add(stockInfo);
            }
            return stockInfoList.size();
        }catch(Exception e){
            //do some
        }finally{
            myBatisCursorItemReader.close();
        }
        return null;
    }

    @GetMapping("/testone")
    public Integer testone() throws Exception {
        try{
//            Map<String,Object> para = new HashMap<>(1);
//            para.put("hasexist","true");
//            myBatisCursorItemReader.setParameterValues(para);
            myBatisPagingItemReader.open(new ExecutionContext());
            myBatisPagingItemReader.setPageSize(10);
            List<StockInfo> stockInfoList = new ArrayList(10);
            StockInfo stockInfo;
            while ((stockInfo=myBatisPagingItemReader.read())!=null) {
                stockInfoList.add(stockInfo);
            }
            return stockInfoList.size();
        }catch(Exception e){
            //do some
        }finally{
            myBatisCursorItemReader.close();
        }
        return null;
    }
}
