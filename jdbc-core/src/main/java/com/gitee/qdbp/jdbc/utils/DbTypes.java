package com.gitee.qdbp.jdbc.utils;

import java.util.List;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 数据库类型判断工具类
 *
 * @author zhaohuihua
 * @version 20200902
 * @since 3.2.0
 */
public abstract class DbTypes {

    public static boolean equals(DbType source, DbType target) {
        VerifyTools.requireNonNull(source, "source");
        return source.name().equalsIgnoreCase(target.name());
    }

    public static boolean equals(DbType source, String target) {
        VerifyTools.requireNonNull(source, "source");
        if (target == null || target.isEmpty()) {
            return false;
        }
        return source.name().equalsIgnoreCase(target);
    }

    public static boolean exists(DbType source, DbType... targets) {
        VerifyTools.requireNonNull(source, "source");
        if (targets == null || targets.length == 0) {
            return false;
        }
        for (DbType target : targets) {
            if (source.name().equalsIgnoreCase(target.name())) {
                return true;
            }
        }
        return false;
    }

    public static boolean exists(DbType source, List<DbType> targets) {
        VerifyTools.requireNonNull(source, "source");
        if (targets == null || targets.isEmpty()) {
            return false;
        }
        for (DbType target : targets) {
            if (source.name().equalsIgnoreCase(target.name())) {
                return true;
            }
        }
        return false;
    }

    public static boolean exists(DbType source, String targets) {
        VerifyTools.requireNonNull(source, "source");
        if (targets == null || targets.isEmpty()) {
            return false;
        }
        String[] array = StringTools.split(targets, ',');
        for (String target : array) {
            if (source.name().equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAvailabl(String dbType) {
        if (dbType == null || dbType.length() == 0) {
            return false;
        }
        List<DbType> dbTypes = DbTools.getAvailableDbTypes();
        for (DbType item : dbTypes) {
            if (equals(item, MainDbType.Unknown)) {
                continue;
            }
            if (item.name().equalsIgnoreCase(dbType)) {
                return true;
            }
        }
        return false;
    }

    public static DbType parse(String dbType) {
        if (dbType == null || dbType.length() == 0) {
            return null;
        }

        List<DbType> dbTypes = DbTools.getAvailableDbTypes();
        for (DbType item : dbTypes) {
            if (equals(item, MainDbType.Unknown)) {
                continue;
            }
            if (item.name().equalsIgnoreCase(dbType)) {
                return item;
            }
        }
        return MainDbType.Unknown;
    }
}
