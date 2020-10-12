package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Map;
import com.gitee.qdbp.jdbc.plugins.BeanToMapConverter;

/**
 * JavaBean到Map的转换<br>
 * 利用fastjson进行JavaBean到Map的转换
 *
 * @author zhaohuihua
 * @version 20200901
 * @since 3.2.0
 */
public class FastJsonBeanToMapConverter implements BeanToMapConverter {

    @Override
    public Map<String, Object> convert(Object bean) {
        return convert(bean, true, true);
    }

    @Override
    public Map<String, Object> convert(Object bean, boolean deep, boolean clearBlankValue) {
        return FastJsonTools.beanToMap(bean, deep, clearBlankValue);
    }
}
