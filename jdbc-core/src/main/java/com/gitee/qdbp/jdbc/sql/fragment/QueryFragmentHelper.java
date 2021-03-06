package com.gitee.qdbp.jdbc.sql.fragment;

import java.util.Collection;
import java.util.List;
import com.gitee.qdbp.able.jdbc.base.WhereCondition;
import com.gitee.qdbp.able.jdbc.condition.DbField;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.fields.Fields;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldException;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.model.FieldScene;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 查询SQL片段生成接口
 *
 * @author zhaohuihua
 * @version 190606
 */
public interface QueryFragmentHelper {

    /**
     * 获取SQL方言处理类
     * 
     * @return SQL方言处理类
     */
    SqlDialect getSqlDialect();

    /**
     * DbWhere转换为Where SQL语句
     * 
     * @param where 一组查询条件
     * @param whole 是否输出完整的WHERE语句, true=带WHERE前缀, false=不带WHERE前缀
     */
    SqlBuffer buildWhereSql(DbWhere where, boolean whole) throws UnsupportedFieldException;

    /**
     * 生成Where SQL语句
     * 
     * @param condition 单字段条件
     * @param whole 是否输出完整的WHERE语句, true=带WHERE前缀, false=不带WHERE前缀
     */
    SqlBuffer buildWhereSql(DbField condition, boolean whole) throws UnsupportedFieldException;

    /**
     * 生成自定义条件的Where SQL语句
     * 
     * @param condition 单字段条件
     * @param whole 是否输出完整的WHERE语句, true=带WHERE前缀, false=不带WHERE前缀
     */
    <T extends WhereCondition> SqlBuffer buildWhereSql(T condition, boolean whole) throws UnsupportedFieldException;

    /**
     * 生成IN语句
     * 
     * @param fieldName 字段名称
     * @param fieldValues 字段值
     * @param whole 是否输出完整的WHERE语句, true=带WHERE前缀, false=不带WHERE前缀
     */
    SqlBuffer buildInSql(String fieldName, Collection<?> fieldValues, boolean whole) throws UnsupportedFieldException;

    /**
     * 生成NOT IN语句
     * 
     * @param fieldName 字段名称
     * @param fieldValues 字段值
     * @param whole 是否输出完整的WHERE语句, true=带WHERE前缀, false=不带WHERE前缀
     */
    SqlBuffer buildNotInSql(String fieldName, Collection<?> fieldValues, boolean whole)
            throws UnsupportedFieldException;

    /**
     * 生成OrderBy SQL语句
     * 
     * @param orderings 排序条件
     * @param whole 是否输出完整的OrderBy语句, true=带ORDER BY前缀, false=不带ORDER BY前缀
     * @return SQL语句
     */
    SqlBuffer buildOrderBySql(Orderings orderings, boolean whole) throws UnsupportedFieldException;

    /**
     * 生成Select字段列表SQL语句
     * 
     * @param fields 只包含指定字段名(如果没有传入参数将生成所有的表字段)
     * @return SQL语句
     */
    SqlBuffer buildSelectFieldsSql(String... fields) throws UnsupportedFieldException;

    /**
     * 生成Select字段列表SQL语句
     * 
     * @param fields 只包含指定字段名(不能为空)
     * @return SQL语句
     */
    SqlBuffer buildSelectFieldsSql(Collection<String> fields) throws UnsupportedFieldException;

    /**
     * 生成Select字段列表SQL语句
     * 
     * @param fields 只包含指定字段名(不能为空)
     * @return SQL语句
     */
    SqlBuffer buildSelectFieldsSql(Fields fields) throws UnsupportedFieldException;

    /**
     * 生成Insert字段列表SQL语句
     * 
     * @param fields 只包含指定字段名(如果没有传入参数将生成所有的表字段)
     * @return SQL语句
     */
    SqlBuffer buildInsertFieldsSql(String... fields) throws UnsupportedFieldException;

    /**
     * 生成Insert字段列表SQL语句
     * 
     * @param fields 只包含指定字段名(不能为空)
     * @return SQL语句
     */
    SqlBuffer buildInsertFieldsSql(Collection<String> fields) throws UnsupportedFieldException;

    /**
     * 生成OrderBy/GroupBy字段列表SQL语句
     * 
     * @param fields 只包含指定字段名(如果没有传入参数将生成所有的表字段)
     * @return SQL语句
     */
    SqlBuffer buildByFieldsSql(String... fields) throws UnsupportedFieldException;

    /**
     * 生成OrderBy/GroupBy字段列表SQL语句
     * 
     * @param fields 只包含指定字段名(不能为空)
     * @return SQL语句
     */
    SqlBuffer buildByFieldsSql(Collection<String> fields) throws UnsupportedFieldException;

    /**
     * 生成OrderBy/GroupBy字段列表SQL语句
     * 
     * @param fields 只包含指定字段名(不能为空)
     * @return SQL语句
     */
    SqlBuffer buildByFieldsSql(Fields fields) throws UnsupportedFieldException;

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
     * 获取所有列信息
     * 
     * @return 列信息
     */
    AllFieldColumn<? extends SimpleFieldColumn> getAllFieldColumns();

    /**
     * 是否存在指定字段
     * 
     * @param scene 字段使用场景
     * @param fieldName 字段名
     * @return 是否存在
     */
    boolean containsField(FieldScene scene, String fieldName);

    /**
     * 获取列名
     * 
     * @param scene 字段使用场景
     * @param fieldName 字段名
     * @return 列名, 如果不支持该字段将抛出异常
     * @throws UnsupportedFieldException 不支持的字段名
     */
    String getColumnName(FieldScene scene, String fieldName) throws UnsupportedFieldException;

    /**
     * 获取列名
     * 
     * @param scene 字段使用场景
     * @param fieldName 字段名
     * @param throwOnUnsupportedField 如果不支持该字段是否抛出异常
     * @return 列名
     * @throws UnsupportedFieldException 不支持的字段名
     */
    String getColumnName(FieldScene scene, String fieldName, boolean throwOnUnsupportedField) throws UnsupportedFieldException;

    /**
     * 获取字段名列表
     * 
     * @param scene 字段使用场景
     * @return 字段名列表
     */
    List<String> getFieldNames(FieldScene scene);

    /**
     * 获取数据库列名列表
     * 
     * @param scene 字段使用场景
     * @return 列名列表
     */
    List<String> getColumnNames(FieldScene scene);

    /**
     * 检查字段名是否在允许范围内
     * 
     * @param scene 字段使用场景
     * @param fields 字段名
     * @param desc 发生错误时的描述内容
     * @throws UnsupportedFieldException 存在不支持的字段名
     */
    void checkSupportedFields(FieldScene scene, Collection<String> fields, String desc) throws UnsupportedFieldException;

    /**
     * 转换特殊的字段值, 如DbFieldName转换为DbRawValue(columnName)
     * 
     * @param fieldValue 字段值
     * @return 转换后的字段值
     */
    Object convertSpecialFieldValue(Object fieldValue);

    /**
     * 转换特殊的字段值, 如DbFieldName转换为DbRawValue(columnName)
     * 
     * @param fieldValues 字段值列表
     * @return 转换后的字段值列表
     */
    List<Object> convertSpecialFieldValues(List<Object> fieldValues);
}
