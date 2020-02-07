package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.ordering.Ordering;
import com.gitee.qdbp.able.jdbc.paging.Paging;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
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
        switch (dbType) {
        case Oracle:
            processPagingForOracle(buffer, paging);
            break;
        case MySQL:
            processPagingForMySql(buffer, paging);
            break;
        case DB2:
            processPagingForDb2(buffer, paging);
            break;
        case H2:
            processPagingForH2(buffer, paging);
            break;
        case PostgreSQL:
            processPagingForPostgreSql(buffer, paging);
            break;
        default:
            throw new UnsupportedOperationException("Unsupported db type: " + dbType);
        }
    }

    protected void processPagingForMySql(SqlBuffer buffer, Paging paging) {
        if (paging.getStart() <= 0) {
            // limit {rows}
            buffer.append('\n').append("LIMIT").append(' ').addVariable(paging.getRows());
        } else {
            // limit {start}, {rows}
            buffer.append('\n').append("LIMIT").append(' ');
            buffer.addVariable(paging.getStart()).append(',').addVariable(paging.getRows());
        }
    }

    protected void processPagingForH2(SqlBuffer buffer, Paging paging) {
        // 逻辑参考自: org.hibernate.dialect.H2Dialect
        if (paging.getStart() <= 0) {
            // limit {rows}
            buffer.append('\n').append("LIMIT").append(' ').addVariable(paging.getRows());
        } else {
            // limit {start} offset {rows}
            buffer.append('\n').append("LIMIT").append(' ');
            buffer.addVariable(paging.getStart());
            buffer.append(" OFFSET ").addVariable(paging.getRows());
        }
    }

    protected void processPagingForPostgreSql(SqlBuffer buffer, Paging paging) {
        // 逻辑参考自: org.hibernate.dialect.PostgreSQLDialect
        if (paging.getStart() <= 0) {
            // limit {rows}
            buffer.append('\n').append("LIMIT").append(' ').addVariable(paging.getRows());
        } else {
            // limit {start} offset {rows}
            buffer.append('\n').append("LIMIT").append(' ');
            buffer.addVariable(paging.getStart());
            buffer.append(" OFFSET ").addVariable(paging.getRows());
        }
    }

    protected void processPagingForOracle(SqlBuffer buffer, Paging paging) {
        // 逻辑参考自: org.hibernate.dialect.OracleDialect
        if (paging.getStart() <= 0) {
            buffer.indent(1, true);
            // SELECT T_T.* FROM (
            //     {sql}
            // ) T_T WHERE ROWNUM <= {end}
            buffer.prepend("SELECT T_T.* FROM (\n");
            buffer.append("\n) T_T\nWHERE ROWNUM <= ").addVariable(paging.getEnd());
        } else {
            buffer.indent(2, true);
            // SELECT * FROM (
            //     SELECT ROWNUM R_N, T_T.* FROM (
            //         {sql}
            //     ) T_T WHERE ROWNUM <= {end}
            // ) WHERE R_N > {start}
            buffer.prepend("SELECT * FROM (\n\tSELECT ROWNUM R_N, T_T.* FROM (\n");
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
            buffer.indent(2, true);
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
            buffer.append('\n', '\t').append("FETCH FIRST").append(' ', end, ' ').append("ROWS ONLY");
            buffer.append('\n', '\t').append(") AS T_T\n)");
            buffer.append(' ').append("WHERE").append(' ').append("R_N > ").append(start);
            buffer.append(' ').append("ORDER BY").append(' ').append("R_N");
        }
    }

    /** {@inheritDoc} **/
    @Override
    public String toPinyinOrderByExpression(String columnName) {
        DbType dbType = dbVersion.getDbType();
        switch (dbType) {
        case Oracle:
            return columnName; // 系统默认排序方式就是拼音: "NLSSORT(" + columnName + ",'NLS_SORT=SCHINESE_PINYIN_M')";
        case MySQL:
            return "CONVERT(" + columnName + " USING GBK)";
        default:
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
        switch (dbType) {
        case DB2:
        case Oracle:
            sb.append("TO_TIMESTAMP").append('(');
            sb.append("'").append(DateTools.toNormativeString(date)).append("'");
            sb.append(',');
            sb.append("'YYYY-MM-DD HH24:MI:SS.FF'");
            sb.append(')');
            return sb.toString();
        case H2:
            sb.append("PARSEDATETIME").append('(');
            sb.append("'").append(DateTools.toNormativeString(date)).append("'");
            sb.append(',');
            sb.append("'yyyy-MM-dd HH:mm:ss.SSS'");
            sb.append(')');
            return sb.toString();
        default:
            sb.append("'").append(DateTools.toNormativeString(date)).append("'");
            return sb.toString();
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildLikeSql(Object fieldValue) {
        DbType dbType = dbVersion.getDbType();
        // TODO chooseEscapeChar
        SqlBuffer buffer = new SqlBuffer();
        buffer.append("LIKE", ' ');
        switch (dbType) {
        case Oracle:
            return buffer.append("('%'||").addVariable(fieldValue).append("||'%')");
        case DB2:
            return buffer.append("('%'||").addVariable(fieldValue).append("||'%')");
        case PostgreSQL:
            return buffer.append("('%'||").addVariable(fieldValue).append("||'%')");
        case MySQL:
            return buffer.append("CONCAT('%',").addVariable(fieldValue).append(",'%')");
        case H2:
            return buffer.append("CONCAT('%',").addVariable(fieldValue).append(",'%')");
        case SqlServer:
            return buffer.append("('%'+").addVariable(fieldValue).append("+'%')");
        default:
            throw new UnsupportedOperationException("Unsupported db type: " + dbType);
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildStartsWithSql(Object fieldValue) {
        DbType dbType = dbVersion.getDbType();
        SqlBuffer buffer = new SqlBuffer();
        buffer.append("LIKE", ' ');
        switch (dbType) {
        case Oracle:
            return buffer.append('(').addVariable(fieldValue).append("||'%')");
        case DB2:
            return buffer.append('(').addVariable(fieldValue).append("||'%')");
        case PostgreSQL:
            return buffer.append('(').addVariable(fieldValue).append("||'%')");
        case MySQL:
            return buffer.append("CONCAT(").addVariable(fieldValue).append(",'%')");
        case H2:
            return buffer.append("CONCAT(").addVariable(fieldValue).append(",'%')");
        case SqlServer:
            return buffer.append('(').addVariable(fieldValue).append("+'%')");
        default:
            throw new UnsupportedOperationException("Unsupported db type: " + dbType);
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildEndsWithSql(Object fieldValue) {
        DbType dbType = dbVersion.getDbType();
        SqlBuffer buffer = new SqlBuffer();
        buffer.append("LIKE", ' ');
        switch (dbType) {
        case Oracle:
            return buffer.append("('%'||").addVariable(fieldValue).append(")");
        case DB2:
            return buffer.append("('%'||").addVariable(fieldValue).append(")");
        case PostgreSQL:
            return buffer.append("('%'||").addVariable(fieldValue).append(")");
        case MySQL:
            return buffer.append("CONCAT('%',").addVariable(fieldValue).append(")");
        case H2:
            return buffer.append("CONCAT('%',").addVariable(fieldValue).append(")");
        case SqlServer:
            return buffer.append("('%'+").addVariable(fieldValue).append(")");
        default:
            throw new UnsupportedOperationException("Unsupported db type: " + dbType);
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildFindChildrenSql(List<String> startCodes, String codeField, String parentField,
            Collection<String> selectFields, DbWhere where, List<Ordering> orderings, QueryFragmentHelper builder) {
        DbType dbType = dbVersion.getDbType();
        VerifyTools.requireNotBlank(startCodes, "startCodes");

        if (dbType == DbType.Oracle) {
            return oracleRecursiveFindChildren(startCodes, codeField, parentField, selectFields, where, orderings, builder);
        } else if (dbType == DbType.MySQL && dbVersion.getMajorVersion() < 8) {
            return productionRecursiveFindChildren(startCodes, codeField, parentField, selectFields, where, orderings, builder);
        } else { // 标准递归语法
            // MySQL8, PostgreSQL的是WITH RECURSIVE; DB2, SqlServer的是WITH, 去掉RECURSIVE即可
            String key;
            if (dbType == DbType.PostgreSQL || dbType == DbType.MySQL) {
                key = "WITH RECURSIVE";
            } else if (dbType == DbType.DB2) {
                key = "WITH";
            } else if (dbType == DbType.SqlServer) {
                key = "WITH";
            } else {
                throw new UnsupportedOperationException("Unsupported db type: " + dbType);
            }
            return normalRecursiveFindChildren(key, startCodes, codeField, parentField, selectFields, where, orderings, builder);
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
            Collection<String> selectFields, DbWhere where, List<Ordering> orderings, QueryFragmentHelper sqlHelper) {

        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT", ' ');
        buffer.append(sqlHelper.buildFieldsSql(selectFields));
        buffer.append(' ', sqlHelper.buildFromSql());
        // START WITH {codeField} IN ( {startCodes} ) 
        buffer.append(' ', "START WITH", ' ').append(sqlHelper.buildInSql(codeField, startCodes, false));
        // CONNECT BY PRIOR {codeField} = {parentField}
        buffer.append(' ', "CONNECT BY PRIOR", ' ');
        buffer.append(sqlHelper.getColumnName(codeField)).append("=").append(sqlHelper.getColumnName(parentField));
        // WHERE ...
        if (where != null && !where.isEmpty()) {
            SqlBuffer whereSql = sqlHelper.buildWhereSql(where, false);
            if (!whereSql.isEmpty()) {
                buffer.append(' ', "AND").append(' ', whereSql);
            }
        }
        // ORDER BY ...
        if (VerifyTools.isNotBlank(orderings)) {
            buffer.append(' ', sqlHelper.buildOrderBySql(orderings, true));
        }
        return buffer;
    }

    /**
     * 标准递归, 语法都差不多, 唯一的区别是关键字: <br>
     * MySQL8, PostgreSQL的是WITH RECURSIVE; DB2, SqlServer的是WITH, 去掉RECURSIVE即可<br>
     * <pre>
    WITH RECURSIVE RECURSIVE_SUB_TABLE(_TEMP_) AS (
        SELECT {codeField} AS _TEMP_ FROM {tableName} WHERE {codeField} IN ( {startCodes} ) 
        UNION ALL
        SELECT {codeField} FROM {tableName} INNER JOIN RECURSIVE_SUB_TABLE ON {parentField} = _TEMP_
    )
    SELECT * FROM {tableName} WHERE {codeField} IN (
        SELECT _TEMP_ FROM RECURSIVE_SUB_TABLE
    )
    WHERE ...
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
            String parentField, Collection<String> selectFields, DbWhere where, List<Ordering> orderings,
            QueryFragmentHelper sqlHelper) {

        // @formatter:off
        String sqlTemplate = "#{keyword} RECURSIVE_SUB_TABLE(_TEMP_) AS (\n"
                + "    SELECT #{codeField} AS _TEMP_ FROM #{tableName} WHERE ${startCodeCondition}\n"
                + "    UNION ALL\n"
                + "    SELECT #{codeField} FROM #{tableName} INNER JOIN RECURSIVE_SUB_TABLE ON #{parentField} = _TEMP_\n"
                + ")\n"
                + "SELECT #{selectFields} FROM #{tableName} WHERE #{codeField} IN (\n"
                + "    SELECT _TEMP_ FROM RECURSIVE_SUB_TABLE\n"
                + ")\n"
                + "${whereCondition}\n"
                + "${orderByCondition} ";
        // @formatter:on

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("codeField", codeField);
        params.put("parentField", parentField);
        params.put("tableName", sqlHelper.buildFromSql());
        params.put("selectFields", sqlHelper.buildFieldsSql(selectFields));
        params.put("startCodeCondition", sqlHelper.buildInSql(codeField, startCodes, false));
        if (where != null && !where.isEmpty()) {
            params.put("whereCondition", sqlHelper.buildWhereSql(where, true));
        }
        if (VerifyTools.isNotBlank(orderings)) {
            params.put("orderByCondition", sqlHelper.buildOrderBySql(orderings, true));
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
            Collection<String> selectFields, DbWhere where, List<Ordering> orderings, QueryFragmentHelper sqlHelper) {

        String selectFieldSql = sqlHelper.buildFieldsSql(selectFields).toString();
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
