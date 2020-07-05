package com.gitee.qdbp.jdbc.plugins.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.DbVersionFinder;

/**
 * 根据数据库连接查找数据库类型和版本信息
 *
 * @author zhaohuihua
 * @version 190705
 */
public abstract class ConnectionDbVersionFinder implements DbVersionFinder {

    protected DbVersion findDbVersion(Connection connection) {
        try {
            DatabaseMetaData metadata = connection.getMetaData();
            // DATABASE_ORACLE, DATABASE_MYSQL, DATABASE_DB2
            String productName = metadata.getDatabaseProductName();
            DbType dbType = parseDbType(productName);
            DbVersion version = new DbVersion();
            version.setDbType(dbType);
            version.setVersionString(metadata.getDatabaseProductVersion());
            version.setMajorVersion(metadata.getDatabaseMajorVersion());
            version.setMinorVersion(metadata.getDatabaseMinorVersion());
            return version;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to get database metadata.", e);
        }
    }

    // DATABASE_ORACLE, DATABASE_MYSQL, DATABASE_DB2
    protected DbType parseDbType(String productName) {
        if (productName == null) {
            return MainDbType.Unknown;
        }
        for (DbType dbType : MainDbType.values()) {
            if (dbType == MainDbType.Unknown) {
                continue;
            }
            if (productName.toUpperCase().contains(dbType.name().toUpperCase())) {
                return dbType;
            }
        }
        return MainDbType.Unknown;
    }
}
