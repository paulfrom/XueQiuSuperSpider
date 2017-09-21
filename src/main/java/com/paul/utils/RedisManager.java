package com.paul.utils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by liusonglin
 * Date:2017/7/26
 * Description:
 */
@Component
public class RedisManager {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 保存到redis
     *
     * @param key
     * @param value
     * @param expire
     * @param <T>
     */
    public <T> void saveRedis(String key, T value, long expire) {
        redisTemplate.opsForValue().set(key, JSON.toJSONString(value), expire, TimeUnit.SECONDS);
    }

    /**
     * 保存到redis
     *
     * @param key
     * @param value
     * @param <T>
     */
    public <T> void saveRedis(String key, T value) {
        redisTemplate.opsForValue().set(key, JSON.toJSONString(value));
    }

    /***
     * redis取值
     * @param key
     * @param t
     * @param <T>
     * @return
     */
    public <T> T getValueByKey(String key, Class<T> t) {
        return JSON.parseObject(redisTemplate.opsForValue().get(key), t);
    }

    /***
     * redis计数器 步长为1
     * @param key
     * @return
     */
    public Long increaseCount(String key) {
        return redisTemplate.opsForValue().increment(key, 1);
    }

    /***
     * redis计数器
     * @param key
     * @param step 步长
     * @return
     */
    public Long increaseCount(String key, long step) {
        return redisTemplate.opsForValue().increment(key, step);
    }

    /**
     * 删除值
     *
     * @param key
     */
    public void delKey(String key) {
        redisTemplate.delete(key);
    }

    /***
     * 发消息
     * @param s
     * @param object
     */
    public void convertAndSend(String s, Object object) {
        redisTemplate.convertAndSend(s, object);
    }


    /**
     * 是否存在
     *
     * @param key
     * @param value
     * @param expire
     */
    public Boolean setIfExist(String key, String value, long expire) {
        boolean result = redisTemplate.opsForValue().setIfAbsent(key, value);
        redisTemplate.expire(key, expire, TimeUnit.SECONDS);
        return result;
    }


    /**
     * 插入集合
     *
     * @param key
     * @param values
     * @param <T>
     * @return
     */
    public <T> int addSet(String key, List<T> values) {
        List<String> valuesTemp = Lists.newArrayListWithCapacity(values.size());
        values.parallelStream().forEach(item -> valuesTemp.add(JSON.toJSONString(item)));
        return redisTemplate.opsForSet().intersect(key, valuesTemp).size();
    }

    /**
     * 单个插入集合
     *
     * @param key
     * @param t
     * @param <T>
     * @return
     */
    public <T> boolean addOneSet(String key, T t) {
        return redisTemplate.opsForSet().add(key, JSON.toJSONString(t)) > 0;
    }

    /**
     * 获取集合
     *
     * @param key
     * @param t
     * @param <T>
     * @return
     */
    public <T> Set<T> getOneFromSet(String key, Class<T> t) {
        Set<String> setTemp = redisTemplate.opsForSet().members(key);
        Set<T> result = Sets.newHashSetWithExpectedSize(setTemp.size());
        setTemp.forEach(item -> result.add(JSON.parseObject(item, t)));
        return result;
    }

    /**
     * 是否在集合里
     *
     * @param key
     * @param t
     * @param <T>
     * @return
     */
    public <T> boolean isInSet(String key, T t) {
        return redisTemplate.opsForSet().isMember(key, JSON.toJSONString(t));
    }

    /***
     * 向map中写入一个实例
     * @param key
     * @param mapKey
     * @param t
     * @param <T>
     */
    public <T> void putMap(String key, String mapKey, T t) {
        redisTemplate.opsForHash().put(key, mapKey, JSON.toJSONString(t));
    }

    /***
     * 从map中获取一个实例
     * @param key
     * @param mapKey
     * @param t
     * @param <T>
     * @return
     */
    public <T> T getFromMap(String key, String mapKey, Class<T> t) {
        return JSON.parseObject(redisTemplate.opsForHash().get(key, mapKey).toString(), t);
    }

    /***
     * 是否在map中
     * @param key
     * @param mapKey
     * @return
     */
    public boolean isInMap(String key, String mapKey) {
        return redisTemplate.opsForHash().hasKey(key,mapKey);
    }

    public Set<Object> getMapKeys(String stock) {
        return redisTemplate.opsForHash().keys(stock);
    }
}
