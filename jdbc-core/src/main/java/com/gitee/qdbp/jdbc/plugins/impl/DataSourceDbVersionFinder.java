package com.gitee.qdbp.jdbc.plugins.impl;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.support.JdbcAccessor;
import com.gitee.qdbp.jdbc.model.DbVersion;

/**
 * 根据数据库查找数据库类型和版本信息
 *
 * @author zhaohuihua
 * @version 190705
 */
public class DataSourceDbVersionFinder extends BaseDbVersionFinder {

    private static Logger log = LoggerFactory.getLogger(DataSourceDbVersionFinder.class);

    @Override
    public DbVersion findDbVersion(JdbcOperations jdbcOperations) {
        if (jdbcOperations instanceof JdbcAccessor) {
            JdbcAccessor accessor = (JdbcAccessor) jdbcOperations;
            DataSource datasource = accessor.getDataSource();
            if (datasource == null) {
                throw new IllegalStateException("Datasource is null.");
            }
            Connection connection = null;
            try {
                connection = datasource.getConnection();
                return findDbVersion(connection);
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to initializing database connection.", e);
            } finally {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        log.error("Exception while closing the database connection", e);
                    }
                }
            }
        }
        throw new IllegalStateException("Unsupported JdbcOperations. " + jdbcOperations.getClass().getName());
    }

}
