package com.gitee.qdbp.jdbc.utils;

import java.io.Serializable;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.tools.utils.Config;

/**
 * 数据库配置容器类
 *
 * @author zhaohuihua
 * @version 20201017
 */
public class DbConfig implements Serializable {

    /** serialVersionUID **/
    private static final long serialVersionUID = 1L;

    private final Config config;
    private final DbVersion version;

    public DbConfig(Config config, DbVersion version) {
        this.config = config;
        this.version = version;
    }

    public Config content() {
        return this.config;
    }

    /**
     * 判断当前数据库是否在配置的支持之列<br>
     * 如: qdbc.recursive.find.children.normal = mysql.8,mariadb.10.2.2,postgresql,db2,sqlserver,sqlite.3.8.3<br>
     * 则 所列出的数据库调用此方法时将返回true; 其他数据库返回false
     * 
     * @param key KEY
     * @return 是否支持
     */
    public boolean supports(String key) {
        return supports(key, null);
    }

    /**
     * 判断当前数据库是否在配置的支持之列<br>
     * 如: qdbc.recursive.find.children.normal = mysql.8,mariadb.10.2.2,postgresql,db2,sqlserver,sqlite.3.8.3<br>
     * 则 所列出的数据库调用此方法时将返回true; 其他数据库返回false
     * 
     * @param key KEY
     * @param defvalue 默认值
     * @return 是否支持
     */
    public boolean supports(String key, String defvalue) {
        String value = config.getStringUseDefKeys(key, defvalue);
        return version.matchesWith(value);
    }

    /**
     * 以key.dbType.version的方式逐级取值<br>
     * 如key = a.b.c, version=mysq.8.0<br>
     * 取值顺序为: a.b.c.mysq.8.0 - a.b.c.mysq.8 - a.b.c.mysq - a.b.c<br>
     * 最终未找到返回null(不会抛出异常)<br>
     *
     * @param key KEY
     * @return VALUE
     */
    public String get(String key) {
        return getString(key);
    }

    /**
     * 以key.dbType.version的方式逐级取值<br>
     * 如key = a.b.c, version=mysq.8.0<br>
     * 取值顺序为: a.b.c.mysq.8.0 - a.b.c.mysq.8 - a.b.c.mysq - a.b.c<br>
     * 最终未找到返回null(不会抛出异常)<br>
     *
     * @param key KEY
     * @return VALUE
     */
    public String getString(String key) {
        return config.getStringUseSuffix(key, version.toVersionString());
    }

    /**
     * 以key.dbType.version的方式逐级取值<br>
     * 如key = a.b.c, version=mysq.8.0<br>
     * 取值顺序为: a.b.c.mysq.8.0 - a.b.c.mysq.8 - a.b.c.mysq - a.b.c<br>
     * 最终未找到返回defvalue<br>
     *
     * @param key KEY
     * @param defvalue 默认值
     * @return VALUE
     */
    public String getString(String key, String defvalue) {
        String value = getString(key);
        return value == null ? defvalue : value;
    }

    /**
     * 以key.dbType.version的方式逐级取值<br>
     * 如key = a.b.c, version=mysq.8.0<br>
     * 取值顺序为: a.b.c.mysq.8.0 - a.b.c.mysq.8 - a.b.c.mysq - a.b.c<br>
     * 最终未找到返回null(不会抛出异常)<br>
     *
     * @param key KEY
     * @return VALUE
     */
    public Long getLong(String key) {
        return getLong(key, null);
    }

    /**
     * 以key.dbType.version的方式逐级取值<br>
     * 如key = a.b.c, version=mysq.8.0<br>
     * 取值顺序为: a.b.c.mysq.8.0 - a.b.c.mysq.8 - a.b.c.mysq - a.b.c<br>
     * 最终未找到返回defvalue<br>
     *
     * @param key KEY
     * @param defvalue 默认值
     * @return VALUE
     */
    public Long getLong(String key, Long defvalue) {
        Long value = config.getLongUseSuffix(key, version.toVersionString());
        return value == null ? defvalue : value;
    }

    /**
     * 以key.dbType.version的方式逐级取值<br>
     * 如key = a.b.c, version=mysq.8.0<br>
     * 取值顺序为: a.b.c.mysq.8.0 - a.b.c.mysq.8 - a.b.c.mysq - a.b.c<br>
     * 最终未找到返回null(不会抛出异常)<br>
     *
     * @param key KEY
     * @return VALUE
     */
    public Integer getInteger(String key) {
        return getInteger(key, null);
    }

    /**
     * 以key.dbType.version的方式逐级取值<br>
     * 如key = a.b.c, version=mysq.8.0<br>
     * 取值顺序为: a.b.c.mysq.8.0 - a.b.c.mysq.8 - a.b.c.mysq - a.b.c<br>
     * 最终未找到返回defvalue<br>
     *
     * @param key KEY
     * @param defvalue 默认值
     * @return VALUE
     */
    public Integer getInteger(String key, Integer defvalue) {
        Integer value = config.getIntegerUseSuffix(key, version.toVersionString());
        return value == null ? defvalue : value;
    }

    /**
     * 以key.dbType.version的方式逐级取值<br>
     * 如key = a.b.c, version=mysq.8.0<br>
     * 取值顺序为: a.b.c.mysq.8.0 - a.b.c.mysq.8 - a.b.c.mysq - a.b.c<br>
     * 最终未找到返回null(不会抛出异常)<br>
     *
     * @param key KEY
     * @return VALUE
     */
    public Double getDouble(String key) {
        return getDouble(key, null);
    }

    /**
     * 以key.dbType.version的方式逐级取值<br>
     * 如key = a.b.c, version=mysq.8.0<br>
     * 取值顺序为: a.b.c.mysq.8.0 - a.b.c.mysq.8 - a.b.c.mysq - a.b.c<br>
     * 最终未找到返回defvalue<br>
     *
     * @param key KEY
     * @param defvalue 默认值
     * @return VALUE
     */
    public Double getDouble(String key, Double defvalue) {
        Double value = config.getDoubleUseSuffix(key, version.toVersionString());
        return value == null ? defvalue : value;
    }

    /**
     * 以key.dbType.version的方式逐级取值<br>
     * 如key = a.b.c, version=mysq.8.0<br>
     * 取值顺序为: a.b.c.mysq.8.0 - a.b.c.mysq.8 - a.b.c.mysq - a.b.c<br>
     * 最终未找到返回null(不会抛出异常)<br>
     *
     * @param key KEY
     * @return VALUE
     */
    public Boolean getBoolean(String key) {
        return getBoolean(key, null);
    }

    /**
     * 以key.dbType.version的方式逐级取值<br>
     * 如key = a.b.c, version=mysq.8.0<br>
     * 取值顺序为: a.b.c.mysq.8.0 - a.b.c.mysq.8 - a.b.c.mysq - a.b.c<br>
     * 最终未找到返回defvalue<br>
     *
     * @param key KEY
     * @param defvalue 默认值
     * @return VALUE
     */
    public Boolean getBoolean(String key, Boolean defvalue) {
        Boolean value = config.getBooleanUseSuffix(key, version.toVersionString());
        return value == null ? defvalue : value;
    }
}
