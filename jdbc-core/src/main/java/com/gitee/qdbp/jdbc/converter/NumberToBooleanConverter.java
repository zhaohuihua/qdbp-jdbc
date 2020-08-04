package com.gitee.qdbp.jdbc.converter;

import org.springframework.core.convert.converter.Converter;

/**
 * 数学转换为Boolean
 *
 * @author zhaohuihua
 * @version 20200606
 */
public class NumberToBooleanConverter implements Converter<Number, Boolean> {

    @Override
    public Boolean convert(Number source) {
        // 0为假, 其他值都为真
        return source.doubleValue() != 0;
    }

}
