package com.gitee.qdbp.jdbc.plugins;

import java.util.Map;

/**
 * Map到JavaBean的转换
 *
 * @author zhaohuihua
 * @version 20200201
 */
public interface MapToBeanConverter {

    /**
     * 将Map转换为Java对象
     * 
     * @param <T> 目标类型
     * @param map Map
     * @param clazz 目标Java类
     * @return Java对象
     */
    <T> T convert(Map<String, ?> map, Class<T> clazz);
}
