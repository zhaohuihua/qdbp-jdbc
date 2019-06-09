package com.gitee.qdbp.jdbc.sql.fragment;

import java.util.Collection;
import java.util.List;
import com.gitee.qdbp.able.model.db.WhereCondition;
import com.gitee.qdbp.able.model.ordering.Ordering;
import com.gitee.qdbp.jdbc.condition.DbField;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldExeption;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 查询SQL片段生成接口
 *
 * @author zhaohuihua
 * @version 190606
 */
public interface QueryFragmentHelper {

    /**
     * DbWhere转换为Where SQL语句
     * 
     * @param where 查询条件
     */
    SqlBuffer buildWhereSql(DbWhere where) throws UnsupportedFieldExeption;

    /**
     * DbWhere转换为Where SQL语句
     * 
     * @param where 查询条件
     * @param whole 是否输出完整的WHERE语句, true=带WHERE前缀, false=不带WHERE前缀
     */
    SqlBuffer buildWhereSql(DbWhere where, boolean whole) throws UnsupportedFieldExeption;

    /**
     * 生成Where SQL语句
     * 
     * @param condition 字段条件
     * @param whole 是否输出完整的WHERE语句, true=带WHERE前缀, false=不带WHERE前缀
     */
    SqlBuffer buildWhereSql(DbField condition, boolean whole) throws UnsupportedFieldExeption;

    /**
     * 生成Where SQL语句
     * 
     * @param condition 字段条件
     * @param whole 是否输出完整的WHERE语句, true=带WHERE前缀, false=不带WHERE前缀
     */
    <T extends WhereCondition> SqlBuffer buildWhereSql(T condition, boolean whole) throws UnsupportedFieldExeption;

    /**
     * 生成IN语句
     * 
     * @param fieldName 字段名称
     * @param fieldValues 字段值
     * @param whole 是否输出完整的WHERE语句, true=带WHERE前缀, false=不带WHERE前缀
     */
    SqlBuffer buildInSql(String fieldName, List<?> fieldValues, boolean matches, boolean whole)
            throws UnsupportedFieldExeption;

    /**
     * 生成NOT IN语句
     * 
     * @param fieldName 字段名称
     * @param fieldValues 字段值
     * @param whole 是否输出完整的WHERE语句, true=带WHERE前缀, false=不带WHERE前缀
     */
    SqlBuffer buildNotInSql(String fieldName, List<?> fieldValues, boolean whole) throws UnsupportedFieldExeption;

    /**
     * 生成OrderBy SQL语句
     * 
     * @param orderings 排序条件
     * @return SQL语句
     */
    SqlBuffer buildOrderBySql(List<Ordering> orderings) throws UnsupportedFieldExeption;

    /**
     * 生成OrderBy SQL语句
     * 
     * @param orderings 排序条件
     * @param whole 是否输出完整的OrderBy语句, true=带ORDER BY前缀, false=不带ORDER BY前缀
     * @return SQL语句
     */
    SqlBuffer buildOrderBySql(List<Ordering> orderings, boolean whole) throws UnsupportedFieldExeption;

    /**
     * 生成Select/Insert字段列表SQL语句
     * 
     * @return SQL语句
     */
    SqlBuffer buildFieldsSql();

    /**
     * 生成Select/Insert字段列表SQL语句
     * 
     * @param fields 只包含指定字段名
     * @return SQL语句
     */
    SqlBuffer buildFieldsSql(String... fields) throws UnsupportedFieldExeption;

    /**
     * 生成Select/Insert字段列表SQL语句
     * 
     * @param fields 只包含指定字段名
     * @return SQL语句
     */
    SqlBuffer buildFieldsSql(Collection<String> fields) throws UnsupportedFieldExeption;

    /**
     * 生成FROM语句<br>
     * 格式: FROM TABLE_NAME
     * 
     * @return SQL语句
     */
    SqlBuffer buildFromSql();

    /**
     * 生成FROM语句<br>
     * 格式: FROM TABLE_NAME
     * 
     * @param whole 是否输出完整的FROM语句, true=带FROM前缀, false=不带FROM前缀
     * @return SQL语句
     */
    SqlBuffer buildFromSql(boolean whole);

    /**
     * 是否存在指定字段
     * 
     * @param fieldName 字段名
     * @return 是否存在
     */
    boolean containsField(String fieldName);

    /**
     * 获取列名
     * 
     * @param fieldName 字段名
     * @return 列名, 如果字段名不存在返回null
     */
    String getColumnName(String fieldName);

    /**
     * 获取列名
     * 
     * @param fieldName 字段名
     * @param throwOnNotFound 如果字段名不存在, 是否抛出异常
     * @return 列名
     * @throws UnsupportedFieldExeption 字段名不存在且throwOnNotFound=true时抛出异常
     */
    String getColumnName(String fieldName, boolean throwOnNotFound) throws UnsupportedFieldExeption;

    /**
     * 获取字段名列表
     * 
     * @return 字段名列表
     */
    List<String> getFieldNames();

    /**
     * 获取数据库列名列表
     * 
     * @return 列名列表
     */
    List<String> getColumnNames();
}
