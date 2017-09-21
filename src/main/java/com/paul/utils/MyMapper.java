package com.paul.utils;

import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * Created by liusonglin
 * Date:2017/9/20
 * Description:
 */
public interface MyMapper<T> extends Mapper<T>,MySqlMapper<T> {
}
