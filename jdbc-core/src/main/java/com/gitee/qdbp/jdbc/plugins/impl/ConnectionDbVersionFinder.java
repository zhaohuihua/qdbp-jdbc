package com.gitee.qdbp.jdbc.plugins.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.DbVersionFinder;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.jdbc.utils.DbTypes;

/**
 * 根据数据库连接查找数据库类型和版本信息
 *
 * @author zhaohuihua
 * @version 190705
 */
public abstract class ConnectionDbVersionFinder implements DbVersionFinder {

    private static Logger log = LoggerFactory.getLogger(ConnectionDbVersionFinder.class);

    protected DbVersion findDbVersion(Connection connection) {
        try {
            DatabaseMetaData metadata = connection.getMetaData();
            // DATABASE_ORACLE, DATABASE_MYSQL, DATABASE_DB2
            String productName = metadata.getDatabaseProductName();
            DbType dbType = parseDbType(productName);
            int majorVersion = metadata.getDatabaseMajorVersion();
            int minorVersion = metadata.getDatabaseMinorVersion();
            DbVersion version = new DbVersion(dbType, majorVersion, minorVersion);
            version.setVersionString(metadata.getDatabaseProductVersion());
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
        List<DbType> dbTypes = DbTools.getAvailableDbTypes();
        for (DbType dbType : dbTypes) {
            if (DbTypes.equals(dbType, MainDbType.Unknown)) {
                continue;
            }
            if (productName.toUpperCase().contains(dbType.name().toUpperCase())) {
                return dbType;
            }
        }
        log.warn("The database type was not recognized, DatabaseProductName={}", productName);
        return MainDbType.Unknown;
    }
}
