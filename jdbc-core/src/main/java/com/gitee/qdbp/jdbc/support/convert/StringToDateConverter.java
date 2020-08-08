package com.gitee.qdbp.jdbc.support.convert;

import java.util.Date;
import org.springframework.core.convert.converter.Converter;
import com.gitee.qdbp.tools.utils.DateTools;

/**
 * 日期转换
 *
 * @author zhaohuihua
 * @version 150303
 */
public class StringToDateConverter implements Converter<String, Date> {

    @Override
    public Date convert(String source) {
        return DateTools.parse(source);
    }

}
