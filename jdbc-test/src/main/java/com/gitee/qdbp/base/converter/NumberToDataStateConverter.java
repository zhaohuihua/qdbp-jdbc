package com.gitee.qdbp.base.converter;

import org.springframework.core.convert.converter.Converter;
import com.gitee.qdbp.jdbc.test.enums.DataState;

/**
 * 数字转换为数据状态, 未找到时返回最大的码值
 *
 * @author zhaohuihua
 * @version 20200725
 */
public class NumberToDataStateConverter implements Converter<Number, DataState> {

    @Override
    public DataState convert(Number source) {
        if (source == null) {
            return null;
        }
        for (DataState item : DataState.values()) {
            if (item.ordinal() == source.doubleValue()) {
                return item;
            }
        }
        // 没找到时返回最大的码值
        return DataState.values()[DataState.values().length - 1];
    }

}
