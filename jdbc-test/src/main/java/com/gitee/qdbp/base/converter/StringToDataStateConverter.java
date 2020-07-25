package com.gitee.qdbp.base.converter;

import org.springframework.core.convert.converter.Converter;
import com.gitee.qdbp.jdbc.test.enums.DataState;
import com.gitee.qdbp.tools.utils.StringTools;

/**
 * 字符串转换为数据状态<br>
 * 如果码值是数字, 未找到时返回最大的码值
 *
 * @author zhaohuihua
 * @version 20200129
 */
public class StringToDataStateConverter implements Converter<String, DataState> {

    @Override
    public DataState convert(String source) {
        if (source == null) {
            return null;
        }
        if (StringTools.isDigit(source)) { // 入参是数字
            for (DataState item : DataState.values()) {
                if (String.valueOf(item.ordinal()).equals(source)) {
                    return item;
                }
            }
            // 没找到时返回最大的码值
            return DataState.values()[DataState.values().length - 1];
        } else { // 入参不是数字
            for (DataState item : DataState.values()) {
                if (item.name().equalsIgnoreCase(source)) {
                    return item;
                }
            }
            // 没找到时返回null
            return null;
        }
    }

}
