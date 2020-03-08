package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Map;
import com.gitee.qdbp.jdbc.plugins.MapToBeanConverter;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 利用fastjson进行Map到JavaBean的转换
 *
 * @author zhaohuihua
 * @version 20200201
 */
public class FastJsonMapToBeanConverter implements MapToBeanConverter {

    @Override
    public <T> T convert(Map<String, ?> map, Class<T> clazz) {
        VerifyTools.requireNonNull(map, "map");
        VerifyTools.requireNonNull(clazz, "class");
        return FastJsonTools.mapToBean(map, clazz);
    }

    @Override
    public <T> void fill(Map<String, ?> map, T bean) {
        VerifyTools.requireNonNull(map, "map");
        VerifyTools.requireNonNull(bean, "bean");
        FastJsonTools.mapFillBean(map, bean);
    }
}
