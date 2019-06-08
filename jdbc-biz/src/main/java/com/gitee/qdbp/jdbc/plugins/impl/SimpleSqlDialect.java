package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.model.ordering.Ordering;
import com.gitee.qdbp.able.model.paging.Paging;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.model.DbType;
import com.gitee.qdbp.jdbc.model.DbVersion;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.fragment.QueryFragmentHelper;
import com.gitee.qdbp.tools.utils.ConvertTools;
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
        if (dbVersion == null) {
            throw new IllegalArgumentException("DbVersion can't be empty");
        }
        this.dbVersion = dbVersion;
    }

    // 参考了org.hibernate.dialect.pagination.LimitHelper类及LimitHandler的子类

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

    private static void processPagingForMySql(SqlBuffer buffer, Paging paging) {
        if (paging.getStart() <= 0) {
            // limit {rows}
            buffer.append(' ').append("LIMIT").append(' ').addVariable("rows", paging.getRows());
        } else {
            // limit {start}, {rows}
            buffer.append(' ').append("LIMIT").append(' ');
            buffer.addVariable("start", paging.getStart()).append(',').addVariable("rows", paging.getRows());
        }
    }

    private static void processPagingForH2(SqlBuffer buffer, Paging paging) {
        // 逻辑参考自: org.hibernate.dialect.H2Dialect
        if (paging.getStart() <= 0) {
            // limit {rows}
            buffer.append(' ').append("LIMIT").append(' ').addVariable("rows", paging.getRows());
        } else {
            // limit {start} offset {rows}
            buffer.append(' ').append("LIMIT").append(' ');
            buffer.addVariable("start", paging.getStart());
            buffer.append(" OFFSET ").addVariable("rows", paging.getRows());
        }
    }

    private static void processPagingForPostgreSql(SqlBuffer buffer, Paging paging) {
        // 逻辑参考自: org.hibernate.dialect.PostgreSQLDialect
        if (paging.getStart() <= 0) {
            // limit {rows}
            buffer.append(' ').append("LIMIT").append(' ').addVariable("rows", paging.getRows());
        } else {
            // limit {start} offset {rows}
            buffer.append(' ').append("LIMIT").append(' ');
            buffer.addVariable("start", paging.getStart());
            buffer.append(" OFFSET ").addVariable("rows", paging.getRows());
        }
    }

    private static void processPagingForOracle(SqlBuffer buffer, Paging paging) {
        // 逻辑参考自: org.hibernate.dialect.OracleDialect
        if (paging.getStart() <= 0) {
            // SELECT T_T.* FROM ( {sql} ) T_T WHERE ROWNUM <= {end}
            buffer.prepend("SELECT T_T.* FROM ( ");
            buffer.append(") T_T WHERE ROWNUM <= ");
            buffer.addVariable("end", paging.getEnd());
        } else {
            // SELECT * FROM (
            //     SELECT ROWNUM R_N, T_T.* FROM ( {sql} ) T_T WHERE ROWNUM <= {end}
            // ) WHERE R_N > {start}
            buffer.prepend("SELECT * FROM ( SELECT ROWNUM R_N, T_T.* FROM ( ");
            buffer.append(") T_T WHERE ROWNUM <= ");
            buffer.addVariable("end", paging.getEnd());
            buffer.append(") WHERE R_N > ");
            buffer.addVariable("start", paging.getStart());
        }
    }

    private static void processPagingForDb2(SqlBuffer buffer, Paging paging) {
        // 逻辑参考自: org.hibernate.dialect.DB2Dialect
        if (paging.getStart() <= 0) {
            // FETCH FIRST {end} ROWS ONLY
            buffer.append(' ').append("FETCH FIRST").append(' ');
            buffer.addVariable("end", paging.getEnd());
            buffer.append(' ').append("ROWS ONLY");
        } else {
            // SELECT * FROM (
            //     SELECT T_T.*, ROWNUMBER() OVER(ORDER BY ORDER OF T_T) AS R_N 
            //     FROM ( {sql} FETCH FIRST {end} ROWS ONLY ) AS T_T
            // )
            // WHERE R_N > {start} ORDER BY R_N
            buffer.prepend("SELECT * FROM ( SELECT T_T.*, ROWNUMBER() OVER(ORDER BY ORDER OF T_T) AS R_N FROM ( ");
            buffer.append(' ').append("FETCH FIRST").append(' ');
            buffer.addVariable("end", paging.getEnd());
            buffer.append(' ').append("ROWS ONLY").append(' ').append(") AS T_T )");
            buffer.append(' ').append("WHERE").append(' ').append("R_N > ");
            buffer.addVariable("start", paging.getStart());
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
    public SqlBuffer buildLikeSql(Object fieldValue) {
        return buildLikeSql(null, fieldValue);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildLikeSql(String fieldName, Object fieldValue) {
        DbType dbType = dbVersion.getDbType();
        // TODO chooseEscapeChar
        SqlBuffer buffer = new SqlBuffer();
        buffer.append("LIKE", ' ');
        switch (dbType) {
        case Oracle:
            return buffer.append("('%'||").addVariable(fieldName, fieldValue).append("||'%')");
        case DB2:
            return buffer.append("('%'||").addVariable(fieldName, fieldValue).append("||'%')");
        case PostgreSQL:
            return buffer.append("('%'||").addVariable(fieldName, fieldValue).append("||'%')");
        case MySQL:
            return buffer.append("CONCAT('%',").addVariable(fieldName, fieldValue).append(",'%')");
        case H2:
            return buffer.append("CONCAT('%',").addVariable(fieldName, fieldValue).append(",'%')");
        case SqlServer:
            return buffer.append("('%'+").addVariable(fieldName, fieldValue).append("+'%')");
        default:
            throw new UnsupportedOperationException("Unsupported db type: " + dbType);
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildStartsWithSql(Object fieldValue) {
        return buildStartsWithSql(null, fieldValue);
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildStartsWithSql(String fieldName, Object fieldValue) {
        DbType dbType = dbVersion.getDbType();
        SqlBuffer buffer = new SqlBuffer();
        buffer.append("LIKE", ' ');
        switch (dbType) {
        case Oracle:
            return buffer.append('(').addVariable(fieldName, fieldValue).append("||'%')");
        case DB2:
            return buffer.append('(').addVariable(fieldName, fieldValue).append("||'%')");
        case PostgreSQL:
            return buffer.append('(').addVariable(fieldName, fieldValue).append("||'%')");
        case MySQL:
            return buffer.append("CONCAT(").addVariable(fieldName, fieldValue).append(",'%')");
        case H2:
            return buffer.append("CONCAT(").addVariable(fieldName, fieldValue).append(",'%')");
        case SqlServer:
            return buffer.append('(').addVariable(fieldName, fieldValue).append("+'%')");
        default:
            throw new UnsupportedOperationException("Unsupported db type: " + dbType);
        }
    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildEndsWithSql(Object fieldValue) {
        return buildEndsWithSql(null, fieldValue);

    }

    /** {@inheritDoc} **/
    @Override
    public SqlBuffer buildEndsWithSql(String fieldName, Object fieldValue) {
        DbType dbType = dbVersion.getDbType();
        SqlBuffer buffer = new SqlBuffer();
        buffer.append("LIKE", ' ');
        switch (dbType) {
        case Oracle:
            return buffer.append("('%'||").addVariable(fieldName, fieldValue).append(")");
        case DB2:
            return buffer.append("('%'||").addVariable(fieldName, fieldValue).append(")");
        case PostgreSQL:
            return buffer.append("('%'||").addVariable(fieldName, fieldValue).append(")");
        case MySQL:
            return buffer.append("CONCAT('%',").addVariable(fieldName, fieldValue).append(")");
        case H2:
            return buffer.append("CONCAT('%',").addVariable(fieldName, fieldValue).append(")");
        case SqlServer:
            return buffer.append("('%'+").addVariable(fieldName, fieldValue).append(")");
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
            return oracleRecursive(startCodes, codeField, parentField, selectFields, where, orderings, builder);
        } else if (dbType == DbType.MySQL && dbVersion.getMajorVersion() < 8) {
            return productionRecursive(startCodes, codeField, parentField, selectFields, where, orderings, builder);
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
            return normalRecursive(key, startCodes, codeField, parentField, selectFields, where, orderings, builder);
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
     * @param sqlBuilder 生成SQL的帮助类
     * @return SQL语句
     */
    private SqlBuffer oracleRecursive(List<String> startCodes, String codeField, String parentField,
            Collection<String> selectFields, DbWhere where, List<Ordering> orderings, QueryFragmentHelper sqlBuilder) {

        SqlBuffer buffer = new SqlBuffer();
        // SELECT ... FROM
        buffer.append("SELECT", ' ');
        buffer.append(sqlBuilder.buildFieldsSql(selectFields));
        buffer.append(' ', sqlBuilder.buildFromSql());
        // START WITH {codeField} IN ( {startCodes} ) 
        buffer.append(' ', "START WITH", ' ').append(sqlBuilder.buildInSql(codeField, startCodes, true, false));
        // CONNECT BY PRIOR {codeField} = {parentField}
        buffer.append(' ', "CONNECT BY PRIOR", ' ');
        buffer.append(sqlBuilder.getColumnName(codeField)).append("=").append(sqlBuilder.getColumnName(parentField));
        // WHERE ...
        if (where != null && !where.isEmpty()) {
            SqlBuffer whereSql = sqlBuilder.buildWhereSql(where, false);
            if (!whereSql.isEmpty()) {
                buffer.append(' ', "AND").append(' ', whereSql);
            }
        }
        // ORDER BY ...
        if (VerifyTools.isNotBlank(orderings)) {
            buffer.append(' ', sqlBuilder.buildOrderBySql(orderings));
        }
        return buffer;
    }

    /**
     * 标准递归, 语法都差不多, 唯一的区别是关键字: <br>
     * MySQL8, PostgreSQL的是WITH RECURSIVE; DB2, SqlServer的是WITH, 去掉RECURSIVE即可<br>
     * <pre>
    WITH RECURSIVE recursive_sub_table(_temp_) AS (
        SELECT {codeField} AS _temp_ FROM {tableName} WHERE {codeField} IN ( {startCodes} ) 
        UNION ALL
        SELECT {codeField} FROM {tableName} INNER JOIN recursive_sub_table ON {parentField} = _temp_
    )
    SELECT * FROM {tableName} WHERE {codeField} IN (
        SELECT _temp_ FROM recursive_sub_table
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
     * @param sqlBuilder 生成SQL的帮助类
     * @return SQL语句
     */
    private SqlBuffer normalRecursive(String keyword, List<String> startCodes, String codeField, String parentField,
            Collection<String> selectFields, DbWhere where, List<Ordering> orderings, QueryFragmentHelper sqlBuilder) {

        // @formatter:off
        String sqlTemplate = "{keyword} recursive_sub_table(_temp_) AS ( "
                + "    SELECT {codeField} AS _temp_ FROM {tableName} WHERE {startCodeCondition} "
                + "    UNION ALL "
                + "    SELECT {codeField} {fromTableName} INNER JOIN recursive_sub_table ON {parentField} = _temp_ "
                + ") "
                + "SELECT {selectFields} FROM {tableName} WHERE {codeField} IN ( "
                + "    SELECT _temp_ FROM recursive_sub_table " + ") " + "{whereCondition:prepend(AND)} "
                + "{orderByCondition:prepend(ORDER BY)} ";
        // @formatter:on

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("codeField", codeField);
        params.put("parentField", parentField);
        params.put("tableName", sqlBuilder.buildFromSql());
        params.put("selectFields", sqlBuilder.buildFieldsSql(selectFields));
        params.put("startCodeCondition", sqlBuilder.buildInSql(codeField, startCodes, true, false));
        if (where != null && !where.isEmpty()) {
            params.put("whereCondition", sqlBuilder.buildWhereSql(where));
        }
        if (VerifyTools.isNotBlank(orderings)) {
            params.put("orderByCondition", sqlBuilder.buildOrderBySql(orderings));
        }
        return SqlBuffer.format(sqlTemplate, params);
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
     * @param sqlBuilder 生成SQL的帮助类
     * @return SQL语句
     */
    private SqlBuffer productionRecursive(List<String> startCodes, String codeField, String parentField,
            Collection<String> selectFields, DbWhere where, List<Ordering> orderings, QueryFragmentHelper sqlBuilder) {

        String selectFieldSql = sqlBuilder.buildFieldsSql(selectFields).toString();
        String whereSql = null;
        if (where != null && !where.isEmpty()) {
            whereSql = sqlBuilder.buildWhereSql(where, false).toString();
        }
        String orderBySql = null;
        if (VerifyTools.isNotBlank(orderings)) {
            orderBySql = sqlBuilder.buildOrderBySql(orderings, false).toString();
        }

        SqlBuffer buffer = new SqlBuffer();
        buffer.append("{CALL RECURSIVE_FIND_CHILDREN", '(');
        buffer.addVariable("tableName", sqlBuilder.buildFromSql(false));
        buffer.append(',');
        buffer.addVariable("startCodes", ConvertTools.joinToString(startCodes));
        buffer.append(',');
        buffer.addVariable("codeField", sqlBuilder.getColumnName(codeField));
        buffer.append(',');
        buffer.addVariable("parentField", sqlBuilder.getColumnName(parentField));
        buffer.append(',');
        buffer.addVariable("selectFields", selectFieldSql);
        buffer.append(',');
        buffer.addVariable("whereSql", whereSql);
        buffer.append(',');
        buffer.addVariable("orderBySql", orderBySql);
        buffer.append(")}");
        return buffer;
    }
}
