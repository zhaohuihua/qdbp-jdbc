package com.gitee.qdbp.jdbc.plugins;

import java.util.Map;

/**
 * JavaBean到Map的转换<br>
 * 注意: 枚举和日期作为基本类型处理, 不作转换!
 *
 * @author zhaohuihua
 * @version 20200901
 * @since 3.2.0
 */
public interface BeanToMapConverter {

    /**
     * 将Java对象转换为Map<br>
     * <b>注意: 枚举和日期作为基本类型处理, 不作转换!</b>
     * 
     * @param bean Java对象
     * @return Map对象
     */
    Map<String, Object> convert(Object bean);

    /**
     * 将Java对象转换为Map<br>
     * <b>注意: 枚举和日期作为基本类型处理, 不作转换!</b>
     * 
     * @param bean Java对象
     * @param deep 是否递归转换子对象
     * @param clearBlankValue 是否清除空值
     * @return Map对象
     */
    Map<String, Object> convert(Object bean, boolean deep, boolean clearBlankValue);

}
