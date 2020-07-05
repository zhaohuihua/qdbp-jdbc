package com.gitee.qdbp.jdbc.plugins.impl;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import com.gitee.qdbp.jdbc.model.DbVersion;

/**
 * 根据数据源查找数据库类型和版本信息
 *
 * @author zhaohuihua
 * @version 190705
 */
public class DataSourceDbVersionFinder extends ConnectionDbVersionFinder {

    @Override
    public DbVersion findDbVersion(DataSource datasource) {
        if (datasource == null) {
            throw new IllegalStateException("Datasource is null.");
        }
        try (Connection connection = datasource.getConnection()) {
            return findDbVersion(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initializing database connection.", e);
        }
    }

}
