package com.gitee.qdbp.jdbc.sql.fragment;

import java.util.Map;
import com.gitee.qdbp.able.jdbc.base.UpdateCondition;
import com.gitee.qdbp.able.jdbc.condition.DbField;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldExeption;
import com.gitee.qdbp.jdbc.model.PrimaryKeyFieldColumn;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;

/**
 * 单表增删改查片段生成接口
 *
 * @author zhaohuihua
 * @version 190606
 */
public interface CrudFragmentHelper extends QueryFragmentHelper {

    /**
     * 获取表名
     * 
     * @return 表名
     */
    String getTableName();

    /**
     * 获取主键
     * 
     * @return 主键, 有可能为空
     */
    PrimaryKeyFieldColumn getPrimaryKey();

    /**
     * 生成Insert字段值占位符列表SQL语句<br>
     * :$1$FieldName, :$2$FieldName, ..., :$n$FieldName
     * 
     * @param entity 字段变量映射表
     * @return SQL语句
     */
    SqlBuffer buildInsertValuesSql(Map<String, Object> entity) throws UnsupportedFieldExeption;

    /**
     * 生成Update字段值占位符列表SQL语句<br>
     * 格式: SET COLUMN_NAME1=:$1$FieldName, COLUMN_NAME2=:$2$FieldName, ..., COLUMN_NAMEn=$n$FieldName<br>
     * 
     * @param entity Update容器对象
     * @param whole 是否输出完整的Update语句, true=带SET前缀, false=不带SET前缀
     * @return SQL语句
     */
    SqlBuffer buildUpdateSetSql(DbUpdate entity, boolean whole) throws UnsupportedFieldExeption;

    /**
     * 生成Update字段值占位符列表SQL语句<br>
     * 格式: SET COLUMN_NAME1=:$1$FieldName<br>
     * 
     * @param field 单字段条件
     * @param whole 是否输出完整的Update语句, true=带SET前缀, false=不带SET前缀
     * @return SQL语句
     */
    SqlBuffer buildUpdateSql(DbField field, boolean whole) throws UnsupportedFieldExeption;

    /**
     * 生成自定义条件的Update SQL语句
     * 
     * @param condition 单字段条件
     * @param whole 是否输出完整的Update语句, true=带SET前缀, false=不带SET前缀
     */
    <T extends UpdateCondition> SqlBuffer buildUpdateSql(T condition, boolean whole) throws UnsupportedFieldExeption;
}
