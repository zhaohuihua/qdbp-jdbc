package com.gitee.qdbp.base.converter;

import org.springframework.core.convert.converter.Converter;
import com.gitee.qdbp.jdbc.test.enums.DataState;

/**
 * 字符串转换为数据状态<br>
 * 0转换为NORMAL, 其他值转换为DELETED
 *
 * @author zhaohuihua
 * @version 200129
 */
public class StringToDataStateConverter implements Converter<String, DataState> {

    @Override
    public DataState convert(String source) {
        if (source == null) {
            return null;
        } else if (String.valueOf(DataState.NORMAL.ordinal()).equals(source)) {
            return DataState.NORMAL;
        } else if (DataState.NORMAL.name().equals(source)) {
            return DataState.NORMAL;
        } else {
            return DataState.DELETED;
        }
    }

}
