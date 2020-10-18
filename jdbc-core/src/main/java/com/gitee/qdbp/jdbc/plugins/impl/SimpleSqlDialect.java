package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.paging.Paging;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.utils.DbTypes;
import com.gitee.qdbp.tools.utils.DateTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 数据库方言处理类
 *
 * @author zhaohuihua
 * @version 190601
 */
public class SimpleSqlDialect implements SqlDialect {

    private DbVersion dbVersion;

    public SimpleSqlDialect(DbVersion dbVersion) {
        VerifyTools.requireNonNull(dbVersion, "DbVersion");
        this.dbVersion = dbVersion;
    }

    /** 获取数据库版本信息 **/
    @Override
    public DbVersion getDbVersion() {
        return dbVersion;
    }

    @Override
    public int getInItemLimit() {
        return DbTypes.equals(dbVersion.getDbType(), MainDbType.Oracle) ? 1000 : 0;
    }

    @Override
    public String rawCurrentTimestamp() {
        DbType dbType = dbVersion.getDbType();
        if (DbTypes.exists(dbType, "Oracle,MySQL,MariaDB,DB2,PostgreSQL")) {
            // 这些数据库都是支持CURRENT_TIMESTAMP的
            return "CURRENT_TIMESTAMP";
        } else if (DbTypes.equals(dbType, MainDbType.SqlServer)) {
            return "GETDATE()";
        } else { // 其他的不知道, 暂时返回CURRENT_TIMESTAMP
            return "CURRENT_TIMESTAMP";
        }
    }

    // 参考了org.hibernate.dialect.pagination.LimitHelper类及LimitHandler的子类

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildPagingSql(SqlBuffer buffer, Paging paging) {
        SqlBuffer copied = buffer.copy();
        processPagingSql(copied, paging);
        return copied;
    }

    /** {@inheritDoc} **/
    @Override
    public void processPagingSql(SqlBuffer buffer, Paging paging) {
        DbType dbType = dbVersion.getDbType();
        if (DbTypes.equals(dbType, MainDbType.Oracle)) {
            processPagingForOracle(buffer, paging);
        } else if (DbTypes.exists(dbType, MainDbType.MySQL, MainDbType.MariaDB)) {
            processPagingForMySql(buffer, paging);
        } else if (DbTypes.equals(dbType, MainDbType.DB2)) {
            processPagingForDb2(buffer, paging);
        } else if (DbTypes.equals(dbType, MainDbType.H2)) {
            processPagingForH2(buffer, paging);
        } else if (DbTypes.equals(dbType, MainDbType.PostgreSQL)) {
            processPagingForPostgreSql(buffer, paging);
        } else if (DbTypes.equals(dbType, MainDbType.SQLite)) {
            processPagingForSqlite(buffer, paging);
        } else {
            // throw new UnsupportedOperationException("Unsupported db type: " + dbType);
            processPagingForLimitOffset(buffer, paging);
        }
    }

    protected void processPagingForMySql(SqlBuffer buffer, Paging paging) {
        SqlBuilder sql = buffer.shortcut();
        if (paging.getStart() <= 0) {
            // limit {rows}
            sql.newline().ad("LIMIT").var(paging.getRows());
        } else {
            // limit {start}, {rows}
            sql.newline().ad("LIMIT").var(paging.getStart()).ad(',').var(paging.getRows());
        }
    }

    protected void processPagingForH2(SqlBuffer buffer, Paging paging) {
        // 逻辑参考自: org.hibernate.dialect.H2Dialect
        processPagingForLimitOffset(buffer, paging);
    }

    protected void processPagingForPostgreSql(SqlBuffer buffer, Paging paging) {
        // 逻辑参考自: org.hibernate.dialect.PostgreSQLDialect
        processPagingForLimitOffset(buffer, paging);
    }

    protected void processPagingForSqlite(SqlBuffer buffer, Paging paging) {
        processPagingForLimitOffset(buffer, paging);
    }

    protected void processPagingForLimitOffset(SqlBuffer buffer, Paging paging) {
        SqlBuilder sql = buffer.shortcut();
        if (paging.getStart() <= 0) {
            // limit {rows}
            sql.newline().ad("LIMIT").var(paging.getRows());
        } else {
            // limit {rows} offset {start}
            sql.newline().ad("LIMIT").var(paging.getRows()).ad("OFFSET").var(paging.getStart());
        }
    }

    protected void processPagingForOracle(SqlBuffer buffer, Paging paging) {
        // 逻辑参考自: org.hibernate.dialect.OracleDialect
        if (paging.getStart() <= 0) {
            buffer.indentAll(1, true);
            // SELECT T_T.* FROM (
            //     {sql}
            // ) T_T WHERE ROWNUM <= {end}
            buffer.prepend("SELECT T_T.* FROM (\n");
            buffer.append("\n) T_T\nWHERE ROWNUM <= ").addVariable(paging.getEnd());
        } else {
            buffer.indentAll(2, true);
            // SELECT * FROM (
            //     SELECT T_T.*, ROWNUM R_N FROM (
            //         {sql}
            //     ) T_T WHERE ROWNUM <= {end}
            // ) WHERE R_N > {start}
            buffer.prepend("SELECT * FROM (\n\tSELECT T_T.*, ROWNUM R_N FROM (\n");
            buffer.append("\n\t) T_T WHERE ROWNUM <= ").addVariable(paging.getEnd());
            buffer.append("\n) WHERE R_N > ").addVariable(paging.getStart());
        }
    }

    // DB2的分页参数不支持占位符
    protected void processPagingForDb2(SqlBuffer buffer, Paging paging) {
        // 逻辑参考自: org.hibernate.dialect.DB2Dialect
        if (paging.getStart() <= 0) {
            // FETCH FIRST {end} ROWS ONLY
            String end = String.valueOf(paging.getEnd());
            buffer.append('\n').append("FETCH FIRST").append(' ', end, ' ').append("ROWS ONLY");
        } else {
            buffer.indentAll(2, true);
            // SELECT * FROM (
            //     SELECT T_T.*, ROWNUMBER() OVER(ORDER BY ORDER OF T_T) AS R_N 
            //     FROM (
            //         {sql}
            //         FETCH FIRST {end} ROWS ONLY
            //     ) AS T_T
            // ) WHERE R_N > {start} ORDER BY R_N
            String start = String.valueOf(paging.getStart());
            String end = String.valueOf(paging.getEnd());
            buffer.prepend("SELECT * FROM (\n\tSELECT T_T.*, ROWNUMBER() OVER(ORDER BY ORDER OF T_T) AS R_N FROM (\n");
            buffer.append('\n', '\t', '\t').append("FETCH FIRST").append(' ', end, ' ').append("ROWS ONLY");
            buffer.append('\n', '\t').append(") AS T_T\n)");
            buffer.append(' ').append("WHERE").append(' ').append("R_N > ").append(start);
            buffer.append(' ').append("ORDER BY").append(' ').append("R_N");
        }
    }

    /** {@inheritDoc} **/
    @Override
    public String toPinyinOrderByExpression(String columnName) {
        DbType dbType = dbVersion.getDbType();
        if (DbTypes.equals(dbType, MainDbType.Oracle)) {
            return columnName; // 系统默认排序方式就是拼音: "NLSSORT(" + columnName + ",'NLS_SORT=SCHINESE_PINYIN_M')";
        } else if (DbTypes.exists(dbType, MainDbType.MySQL, MainDbType.MariaDB)) {
            return "CONVERT(" + columnName + " USING GBK)";
        } else {
            return columnName;
        }
    }

    /** {@inheritDoc} **/
    @Override
    public String variableToString(Boolean variable) {
        return String.valueOf(Boolean.TRUE.equals(variable) ? 1 : 0);
    }

    /** {@inheritDoc} **/
    @Override
    public String variableToString(String variable) {
        return new StringBuilder().append("'").append(variable.replace("'", "''")).append("'").toString();
    }

    /** {@inheritDoc} **/
    @Override
    public String variableToString(Date date) {
        StringBuilder sb = new StringBuilder();
        DbType dbType = dbVersion.getDbType();
        if (DbTypes.exists(dbType, MainDbType.Oracle, MainDbType.DB2)) {
            sb.append("TO_TIMESTAMP").append('(');
            sb.append("'").append(DateTools.toNormativeString(date)).append("'");
            sb.append(',');
            sb.append("'YYYY-MM-DD HH24:MI:SS.FF'");
            sb.append(')');
            return sb.toString();
        } else if (DbTypes.equals(dbType, MainDbType.H2)) {
            sb.append("PARSEDATETIME").append('(');
            sb.append("'").append(DateTools.toNormativeString(date)).append("'");
            sb.append(',');
            sb.append("'yyyy-MM-dd HH:mm:ss.SSS'");
            sb.append(')');
            return sb.toString();
        } else {
            sb.append("'").append(DateTools.toNormativeString(date)).append("'");
            return sb.toString();
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildLikeSql(Object fieldValue) {
        DbType dbType = dbVersion.getDbType();
        // TODO chooseEscapeChar
        if (DbTypes.exists(dbType, MainDbType.Oracle, MainDbType.DB2, MainDbType.PostgreSQL)) {
            return new SqlBuilder("LIKE").ad("('%'||").var(fieldValue).ad("||'%')").out();
        } else if (DbTypes.exists(dbType, MainDbType.MySQL, MainDbType.MariaDB, MainDbType.H2)) {
            return new SqlBuilder("LIKE").ad("CONCAT('%',").var(fieldValue).ad(",'%')").out();
        } else if (DbTypes.equals(dbType, MainDbType.SqlServer)) {
            return new SqlBuilder("LIKE").ad("('%'+").var(fieldValue).ad("+'%')").out();
        } else {
            return new SqlBuilder("LIKE").ad("('%'||").var(fieldValue).ad("||'%')").out();
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildStartsWithSql(Object fieldValue) {
        DbType dbType = dbVersion.getDbType();
        if (DbTypes.exists(dbType, MainDbType.Oracle, MainDbType.DB2, MainDbType.PostgreSQL)) {
            return new SqlBuilder("LIKE").ad('(').var(fieldValue).ad("||'%')").out();
        } else if (DbTypes.exists(dbType, MainDbType.MySQL, MainDbType.MariaDB, MainDbType.H2)) {
            return new SqlBuilder("LIKE").ad("CONCAT(").var(fieldValue).ad(",'%')").out();
        } else if (DbTypes.equals(dbType, MainDbType.SqlServer)) {
            return new SqlBuilder("LIKE").ad('(').var(fieldValue).ad("+'%')").out();
        } else {
            return new SqlBuilder("LIKE").ad('(').var(fieldValue).ad("||'%')").out();
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildEndsWithSql(Object fieldValue) {
        DbType dbType = dbVersion.getDbType();
        if (DbTypes.exists(dbType, MainDbType.Oracle, MainDbType.DB2, MainDbType.PostgreSQL)) {
            return new SqlBuilder("LIKE").ad("('%'||").var(fieldValue).ad(")").out();
        } else if (DbTypes.exists(dbType, MainDbType.MySQL, MainDbType.MariaDB, MainDbType.H2)) {
            return new SqlBuilder("LIKE").ad("CONCAT('%',").var(fieldValue).ad(")").out();
        } else if (DbTypes.equals(dbType, MainDbType.SqlServer)) {
            return new SqlBuilder("LIKE").ad("('%'+").var(fieldValue).ad(")").out();
        } else {
            return new SqlBuilder("LIKE").ad("('%'||").var(fieldValue).ad(")").out();
        }
    }

    /**
     * SqlDialect创建接口的SimpleSqlDialect实现类
     *
     * @author zhaohuihua
     * @version 20201018
     */
    public static class Creator implements SqlDialect.Creator {

        @Override
        public SqlDialect create(DbVersion version) {
            return buildSqlDialect(version);
        }

        // key=DbVersionCode
        private Map<String, SqlDialect> sqlDialectMaps = new HashMap<>();

        /** 根据DbVersion生成SqlDialect **/
        public SqlDialect buildSqlDialect(DbVersion version) {
            String versionCode = version.toVersionString();
            if (sqlDialectMaps.containsKey(versionCode)) {
                return sqlDialectMaps.get(versionCode);
            } else {
                SqlDialect dialect = new SimpleSqlDialect(version);
                sqlDialectMaps.put(versionCode, dialect);
                return dialect;
            }
        }
    }
}
