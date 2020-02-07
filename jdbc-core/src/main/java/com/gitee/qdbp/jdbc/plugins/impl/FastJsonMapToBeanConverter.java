package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Map;
import com.gitee.qdbp.jdbc.plugins.MapToBeanConverter;

/**
 * 利用fastjson进行Map到JavaBean的转换
 *
 * @author zhaohuihua
 * @version 200201
 */
public class FastJsonMapToBeanConverter implements MapToBeanConverter {

    @Override
    public <T> T convert(Map<String, ?> map, Class<T> clazz) {
        return FastJsonTools.mapToBean(map, clazz);
    }
}
