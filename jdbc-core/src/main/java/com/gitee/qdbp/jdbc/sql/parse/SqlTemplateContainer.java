package com.gitee.qdbp.jdbc.sql.parse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.gitee.qdbp.staticize.common.IMetaData;

class SqlTemplateContainer {

    private static final SqlTemplateContainer DEFAULTS = new SqlTemplateContainer();

    public static SqlTemplateContainer defaults() {
        return DEFAULTS;
    }

    private Map<String, IMetaData> cache = new ConcurrentHashMap<>();

    private SqlTemplateContainer() {
    }

    public void registerSqlData(String sqlId, IMetaData data) {
        this.cache.put(sqlId, data);
    }

    public void registerSqlData(Map<String, IMetaData> datas) {
        this.cache.putAll(datas);
    }

    public IMetaData getSqlData(String sqlId) {
        return this.cache.get(sqlId);
    }
}
