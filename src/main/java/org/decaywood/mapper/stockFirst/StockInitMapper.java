package org.decaywood.mapper.stockFirst;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.paul.mapper.StockInfoMapper;
import com.paul.utils.RedisManager;
import lombok.extern.slf4j.Slf4j;
import org.decaywood.entity.StockInfo;
import org.decaywood.mapper.AbstractMapper;
import org.decaywood.timeWaitingStrategy.TimeWaitingStrategy;
import org.decaywood.utils.RequestParaBuilder;
import org.decaywood.utils.StringUtils;
import org.decaywood.utils.URLMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by liusonglin
 * Date:2017/9/20
 * Description:
 */
@Component
@Slf4j
public class StockInitMapper extends AbstractMapper<List<StockInfo>, List<StockInfo>> {

    @Autowired
    private StockInfoMapper stockInfoMapper;

    @Autowired
    private RedisManager redisManager;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * @param strategy 超时等待策略（null则设置为默认等待策略）
     */
    public StockInitMapper(TimeWaitingStrategy strategy) throws RemoteException {
        super(strategy);
    }

    public StockInitMapper() throws RemoteException {
        this(null);
    }


    @Override
    public List<StockInfo> mapLogic(List<StockInfo> stock) throws Exception {

        DecimalFormat decimalFormat = new DecimalFormat("00000");

        int code = 1;
        while (code != 291) {
            Thread.sleep(1000);
            code=code+1;
            try {
                String target = URLMapper.SEARCH_STOCK.toString();
                RequestParaBuilder builder = new RequestParaBuilder(target)
                        .addParameter("size", 10).addParameter("code", "sz"+decimalFormat.format(code));
                URL url = new URL(builder.build());
                log.info("url is : {}",url);
                String json = requestGet(url);
                if(StringUtils.nullOrEmpty(json)){
                    continue;
                }
                List<StockInfo> stockInfoList = Lists.newArrayListWithCapacity(100);
                List<JSONObject> stockInfos = (List) JSON.parseObject(json).get("stocks");
                log.info("stock count is : {}",stockInfos.size());
                stockInfos.parallelStream()
                        .filter(item -> item.getString("name").length() <= 32)
                        .filter(item -> !redisManager.isInMap("stock", item.getString("code")))
                        .forEach(item -> {
                            StockInfo stockInfo = new StockInfo();
                            stockInfo.setCode(item.getString("code"));
                            stockInfo.setName(item.getString("name"));
                            stockInfo.setInd_id(item.getString("ind_id"));
                            stockInfo.setEnName(item.getString("enName"));
                            stockInfo.setInd_name(item.getString("ind_name"));
                            stockInfo.setStock_id(item.getString("stock_id"));
                            stockInfo.setHasexist(item.getString("hasexist"));
                            stockInfo.setType(item.getString("type"));
                            stockInfo.setFlag(item.getString("flag"));
                            log.info("item is {}:",JSON.toJSONString(stockInfo));
                            stockInfoList.add(stockInfo);
                            redisManager.putMap("stock", stockInfo.getCode(),stockInfo);
                        });

                if (!stockInfoList.isEmpty()) {
                    stockInfoMapper.insertList(stockInfoList);
                }
            }catch (Exception e){
                log.error("init error",e);
            }
        }
        List<String> temp = redisTemplate.opsForHash().values("stock");
        return Arrays.asList(temp.toArray(new StockInfo[temp.size()]));
    }

}
