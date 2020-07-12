package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.jdbc.plugins.RawValueConverter;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.utils.InnerTools;

/**
 * 数据库原生值的转换处理类(sysdate, CURRENT_TIMESTAMP等)
 *
 * @author zhaohuihua
 * @version 20200709
 */
public class SimpleRawValueConverter implements RawValueConverter {

    private Map<String, Void> currentTimestampMaps = new HashMap<>();

    public SimpleRawValueConverter() {
        this.addCurrentTimestamps("sysdate,CURRENT_DATE,CURRENT_TIMESTAMP");
    }

    public void setCurrentTimestamps(String currentTimestamps) {
        this.setCurrentTimestamps(InnerTools.tokenizeToStringList(currentTimestamps));
    }

    public void setCurrentTimestamps(List<String> currentTimestamps) {
        currentTimestampMaps.clear();
        for (String item : currentTimestamps) {
            currentTimestampMaps.put(item.toUpperCase(), null);
        }
    }

    public void addCurrentTimestamps(String currentTimestamps) {
        this.addCurrentTimestamps(InnerTools.tokenizeToStringList(currentTimestamps));
    }

    public void addCurrentTimestamps(List<String> currentTimestamps) {
        for (String item : currentTimestamps) {
            currentTimestampMaps.put(item.toUpperCase(), null);
        }
    }

    @Override
    public String convert(String value, SqlDialect dialect) {
        if ("true".equalsIgnoreCase(value)) {
            return "1";
        } else if ("false".equalsIgnoreCase(value)) {
            return "0";
        } else if (currentTimestampMaps.containsKey(value.toUpperCase())) {
            return dialect.rawCurrentTimestamp();
        }
        return value;
    }

}
