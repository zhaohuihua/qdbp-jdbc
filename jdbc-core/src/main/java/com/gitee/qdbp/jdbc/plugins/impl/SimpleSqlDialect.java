package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.able.jdbc.paging.Paging;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.model.MainDbType;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.jdbc.sql.fragment.QueryFragmentHelper;
import com.gitee.qdbp.jdbc.sql.mapper.SqlParser;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
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
        DbType dbType = dbVersion.getDbType();
        return dbType == MainDbType.Oracle ? 1000 : 0;
    }

    @Override
    public String rawCurrentTimestamp() {
        DbType dbType = dbVersion.getDbType();
        if (dbType == MainDbType.Oracle || dbType == MainDbType.MySQL || dbType == MainDbType.MariaDB
                || dbType == MainDbType.DB2 || dbType == MainDbType.PostgreSQL) {
            // 这些数据库都是支持CURRENT_TIMESTAMP的
            return "CURRENT_TIMESTAMP";
        } else if (dbType == MainDbType.SqlServer) {
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
        if (dbType == MainDbType.Oracle) {
            processPagingForOracle(buffer, paging);
        } else if (dbType == MainDbType.MySQL || dbType == MainDbType.MariaDB) {
            processPagingForMySql(buffer, paging);
        } else if (dbType == MainDbType.DB2) {
            processPagingForDb2(buffer, paging);
        } else if (dbType == MainDbType.H2) {
            processPagingForH2(buffer, paging);
        } else if (dbType == MainDbType.PostgreSQL) {
            processPagingForPostgreSql(buffer, paging);
        } else if (dbType == MainDbType.SQLite) {
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
        if (dbType == MainDbType.Oracle) {
            return columnName; // 系统默认排序方式就是拼音: "NLSSORT(" + columnName + ",'NLS_SORT=SCHINESE_PINYIN_M')";
        } else if (dbType == MainDbType.MySQL || dbType == MainDbType.MariaDB) {
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
        if (dbType == MainDbType.Oracle || dbType == MainDbType.DB2) {
            sb.append("TO_TIMESTAMP").append('(');
            sb.append("'").append(DateTools.toNormativeString(date)).append("'");
            sb.append(',');
            sb.append("'YYYY-MM-DD HH24:MI:SS.FF'");
            sb.append(')');
            return sb.toString();
        } else if (dbType == MainDbType.H2) {
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
        if (dbType == MainDbType.Oracle || dbType == MainDbType.DB2 || dbType == MainDbType.PostgreSQL) {
            return new SqlBuilder("LIKE").ad("('%'||").var(fieldValue).ad("||'%')").out();
        } else if (dbType == MainDbType.MySQL || dbType == MainDbType.MariaDB || dbType == MainDbType.H2) {
            return new SqlBuilder("LIKE").ad("CONCAT('%',").var(fieldValue).ad(",'%')").out();
        } else if (dbType == MainDbType.SqlServer) {
            return new SqlBuilder("LIKE").ad("('%'+").var(fieldValue).ad("+'%')").out();
        } else {
            return new SqlBuilder("LIKE").ad("('%'||").var(fieldValue).ad("||'%')").out();
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildStartsWithSql(Object fieldValue) {
        DbType dbType = dbVersion.getDbType();
        if (dbType == MainDbType.Oracle || dbType == MainDbType.DB2 || dbType == MainDbType.PostgreSQL) {
            return new SqlBuilder("LIKE").ad('(').var(fieldValue).ad("||'%')").out();
        } else if (dbType == MainDbType.MySQL || dbType == MainDbType.MariaDB || dbType == MainDbType.H2) {
            return new SqlBuilder("LIKE").ad("CONCAT(").var(fieldValue).ad(",'%')").out();
        } else if (dbType == MainDbType.SqlServer) {
            return new SqlBuilder("LIKE").ad('(').var(fieldValue).ad("+'%')").out();
        } else {
            return new SqlBuilder("LIKE").ad('(').var(fieldValue).ad("||'%')").out();
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildEndsWithSql(Object fieldValue) {
        DbType dbType = dbVersion.getDbType();
        if (dbType == MainDbType.Oracle || dbType == MainDbType.DB2 || dbType == MainDbType.PostgreSQL) {
            return new SqlBuilder("LIKE").ad("('%'||").var(fieldValue).ad(")").out();
        } else if (dbType == MainDbType.MySQL || dbType == MainDbType.MariaDB || dbType == MainDbType.H2) {
            return new SqlBuilder("LIKE").ad("CONCAT('%',").var(fieldValue).ad(")").out();
        } else if (dbType == MainDbType.SqlServer) {
            return new SqlBuilder("LIKE").ad("('%'+").var(fieldValue).ad(")").out();
        } else {
            return new SqlBuilder("LIKE").ad("('%'||").var(fieldValue).ad(")").out();
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildFindChildrenSql(List<String> startCodes, String codeField, String parentField,
            Collection<String> selectFields, DbWhere where, Orderings orderings, QueryFragmentHelper builder) {
        DbType dbType = dbVersion.getDbType();
        VerifyTools.requireNotBlank(startCodes, "startCodes");

        if (dbType == MainDbType.Oracle) {
            return oracleRecursiveFindChildren(startCodes, codeField, parentField, selectFields, where, orderings,
                builder);
        } else if (dbType == MainDbType.MySQL && dbVersion.getMajorVersion() < 8) {
            return productionRecursiveFindChildren(startCodes, codeField, parentField, selectFields, where, orderings,
                builder);
        } else if (dbType == MainDbType.MariaDB && dbVersion.versionCompareTo("10.2.2") < 0) {
            // 听说MariaDB 10.2.2才开始提供递归语法
            // https://mariadb.com/kb/en/mariadb-1022-release-notes/
            // Recursive Common Table Expressions
            return productionRecursiveFindChildren(startCodes, codeField, parentField, selectFields, where, orderings,
                builder);
        } else { // 标准递归语法
            // MySQL8, PostgreSQL的是WITH RECURSIVE; DB2, SqlServer的是WITH, 去掉RECURSIVE即可
            String key;
            if (dbType == MainDbType.PostgreSQL || dbType == MainDbType.MySQL || dbType == MainDbType.MariaDB
                    || dbType == MainDbType.SQLite) {
                key = "WITH RECURSIVE";
            } else if (dbType == MainDbType.DB2 || dbType == MainDbType.SqlServer) {
                key = "WITH";
            } else {
                // throw new UnsupportedOperationException("Unsupported db type: " + dbType);
                key = "WITH RECURSIVE";
            }
            return normalRecursiveFindChildren(key, startCodes, codeField, parentField, selectFields, where, orderings,
                builder);
        }
    }

    /**
     * Oracle递归语法<br>
     * <pre>
    SELECT * FROM {tableName} 
    START WITH {codeField} IN ( {startCodes} ) 
    CONNECT BY PRIOR {codeField} = {parentField}
    WHERE ...
    ORDER BY ...
     * </pre>
     * 
     * @param startCodes 起始编号
     * @param codeField 编号字段
     * @param parentField 上级编号字段
     * @param selectFields 查询字段列表
     * @param where 查询条件
     * @param orderings 排序条件
     * @param sqlHelper 生成SQL的帮助类
     * @return SQL语句
     */
    protected SqlBuffer oracleRecursiveFindChildren(List<String> startCodes, String codeField, String parentField,
            Collection<String> selectFields, DbWhere where, Orderings orderings, QueryFragmentHelper sqlHelper) {
        SqlBuilder sql = new SqlBuilder();
        // SELECT ... FROM
        sql.ad("SELECT");
        sql.ad(sqlHelper.buildSelectFieldsSql(selectFields));
        sql.ad(sqlHelper.buildFromSql());
        // START WITH {codeField} IN ( {startCodes} ) 
        sql.ad("START WITH").ad(sqlHelper.buildInSql(codeField, startCodes, false));
        // CONNECT BY PRIOR {codeField} = {parentField}
        sql.ad("CONNECT BY PRIOR");
        sql.ad(sqlHelper.getColumnName(codeField)).ad("=").ad(sqlHelper.getColumnName(parentField));
        // WHERE ...
        if (where != null && !where.isEmpty()) {
            SqlBuffer whereSql = sqlHelper.buildWhereSql(where, false);
            if (!whereSql.isEmpty()) {
                sql.ad("AND").ad(whereSql);
            }
        }
        // ORDER BY ...
        if (VerifyTools.isNotBlank(orderings)) {
            sql.ad(sqlHelper.buildOrderBySql(orderings, true));
        }
        return sql.out();
    }

    /**
     * 标准递归, 语法都差不多, 唯一的区别是关键字: <br>
     * MySQL8, PostgreSQL的是WITH RECURSIVE; DB2, SqlServer的是WITH, 去掉RECURSIVE即可<br>
     * <pre>
    WITH RECURSIVE recursive_temp_table(_TEMP_) AS (
        SELECT {codeField} AS _TEMP_ FROM {tableName} WHERE {codeField} IN ( {startCodes} ) 
        UNION ALL
        SELECT {codeField} FROM {tableName} INNER JOIN recursive_temp_table ON {parentField} = _TEMP_
    )
    SELECT {selectFields} FROM {tableName} WHERE {codeField} IN (
        SELECT _TEMP_ FROM recursive_temp_table
    )
    WHERE ...
    ORDER BY ...
     * </pre> <pre>
    -- 实测发现DB2不支持_开头的字段名, 不支持field AS alias, WITH AS中不支持INNER JOIN
    WITH recursive_temp_table(temp_parent) AS (
        SELECT {codeField} temp_parent FROM {tableName} WHERE {codeField} IN ( {startCodes} ) 
        UNION ALL
        SELECT {codeField} FROM {tableName} A, recursive_temp_table B ON A.{parentField} = B.temp_parent
    )
    SELECT {selectFields} FROM {tableName} WHERE {codeField} IN (
        SELECT temp_parent FROM recursive_temp_table
    )
    AND ...
    ORDER BY ...
     * </pre>
     * 
     * @param keyword 递归关键字
     * @param startCodes 起始编号
     * @param codeField 编号字段
     * @param parentField 上级编号字段
     * @param selectFields 查询字段列表
     * @param where 查询条件
     * @param orderings 排序条件
     * @param sqlHelper 生成SQL的帮助类
     * @return SQL语句
     */
    protected SqlBuffer normalRecursiveFindChildren(String keyword, List<String> startCodes, String codeField,
            String parentField, Collection<String> selectFields, DbWhere where, Orderings orderings,
            QueryFragmentHelper sqlHelper) {

        // @formatter:off
        String sqlTemplate = "#{keyword} recursive_temp_table(temp_parent) AS (\n"
                + "    SELECT #{codeField} temp_parent FROM #{tableName} WHERE ${startCodeCondition}\n"
                + "    UNION ALL\n"
                + "    SELECT #{codeField} FROM #{tableName} A, recursive_temp_table B ON A.#{parentField} = B.temp_parent\n"
                + ")\n" + "SELECT #{selectFields} FROM #{tableName} WHERE #{codeField} IN (\n"
                + "    SELECT temp_parent FROM recursive_temp_table\n" + ")\n" + "${whereCondition}\n"
                + "#{orderByCondition} ";
        // @formatter:on

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("codeField", codeField);
        params.put("parentField", parentField);
        params.put("tableName", sqlHelper.buildFromSql());
        params.put("selectFields", sqlHelper.buildSelectFieldsSql(selectFields));
        params.put("startCodeCondition", sqlHelper.buildInSql(codeField, startCodes, false));
        if (where != null && !where.isEmpty()) {
            params.put("whereCondition", sqlHelper.buildWhereSql(where, false));
        }
        if (VerifyTools.isNotBlank(orderings)) {
            params.put("orderByCondition", sqlHelper.buildOrderBySql(orderings, false));
        }
        SqlParser parser = DbTools.buildSqlParser(this);
        return parser.parse(sqlTemplate, params);
    }

    /**
     * 使用存储过程递归查询所有子节点<br>
     * CALL RECURSIVE_FIND_CHILDREN(startCodes,codeField,parentField,tableName,selectFields,whereSql,orderBySql)<br>
     * <pre>
     * 参数示例:
     * startCodes: "340000,350000",
     * codeField: "area_code",
     * parentField: "parent_code",
     * tableName: "comm_area_division",
     * selectFields: null,
     * whereSql: "type="default" AND data_state=0"
     * orderBySql: "parent_code ASC, sort_index ASC"
     * </pre>
     * 
     * @param startCodes 起始编号
     * @param codeField 编号字段
     * @param parentField 上级编号字段
     * @param selectFields 查询字段列表
     * @param where 查询条件
     * @param orderings 排序条件
     * @param sqlHelper 生成SQL的帮助类
     * @return SQL语句
     */
    protected SqlBuffer productionRecursiveFindChildren(List<String> startCodes, String codeField, String parentField,
            Collection<String> selectFields, DbWhere where, Orderings orderings, QueryFragmentHelper sqlHelper) {

        String selectFieldSql = sqlHelper.buildSelectFieldsSql(selectFields).toString();
        String whereSql = null;
        if (where != null && !where.isEmpty()) {
            whereSql = sqlHelper.buildWhereSql(where, false).toString();
        }
        String orderBySql = null;
        if (VerifyTools.isNotBlank(orderings)) {
            orderBySql = sqlHelper.buildOrderBySql(orderings, false).toString();
        }

        SqlBuffer buffer = new SqlBuffer();
        buffer.append("{CALL RECURSIVE_FIND_CHILDREN", '(');
        buffer.addVariable(sqlHelper.buildFromSql(false)); // tableName
        buffer.append(',');
        buffer.addVariable(ConvertTools.joinToString(startCodes));
        buffer.append(',');
        buffer.addVariable(sqlHelper.getColumnName(codeField));
        buffer.append(',');
        buffer.addVariable(sqlHelper.getColumnName(parentField));
        buffer.append(',');
        buffer.addVariable(selectFieldSql);
        buffer.append(',');
        buffer.addVariable(whereSql);
        buffer.append(',');
        buffer.addVariable(orderBySql);
        buffer.append(")}");
        return buffer;
    }
}
