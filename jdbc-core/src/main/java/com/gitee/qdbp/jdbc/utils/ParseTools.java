package com.gitee.qdbp.jdbc.utils;

import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.plugins.DbConditionConverter;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 解析工具类
 *
 * @author zhaohuihua
 * @version 190703
 */
public class ParseTools {

    /**
     * 将JavaBean解析为DbWhere对象
     * 
     * @param bean Java对象
     * @return DbWhere
     */
    public static DbWhere parseBeanToDbWhere(Object bean) {
        return parseBeanToDbWhere(bean, true);
    }

    /**
     * 将JavaBean解析为DbWhere对象
     * 
     * @param bean Java对象
     * @param emptiable 是否允许DbWhere对象为空
     * @return DbWhere
     */
    public static DbWhere parseBeanToDbWhere(Object bean, boolean emptiable) {
        if (!emptiable) {
            VerifyTools.requireNonNull(bean, "bean");
        }
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        DbWhere where = converter.convertBeanToDbWhere(bean);
        if (!emptiable && where.isEmpty()) {
            throw new IllegalArgumentException("bean must not be empty.");
        }
        return where;
    }

    /**
     * 将JavaBean解析为DbUpdate对象
     * 
     * @param bean Java对象
     * @return DbUpdate
     */
    public static DbUpdate parseBeanToDbUpdate(Object bean) {
        return parseBeanToDbUpdate(bean, true);
    }

    /**
     * 将JavaBean解析为DbUpdate对象
     * 
     * @param bean Java对象
     * @param emptiable 是否允许DbUpdate对象为空
     * @return DbUpdate
     */
    public static DbUpdate parseBeanToDbUpdate(Object bean, boolean emptiable) {
        if (!emptiable) {
            VerifyTools.requireNonNull(bean, "bean");
        }
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        DbUpdate ud = converter.convertBeanToDbUpdate(bean);
        if (!emptiable && ud.isEmpty()) {
            throw new IllegalArgumentException("bean must not be empty.");
        }
        return ud;
    }

    /**
     * 从请求参数request.getParameterMap()中构建Where对象<br>
     * 只会包含clazz注解中通过@JoyInColumn指定的字段名<br>
     * 应注意, 此时参数由前端传入, 条件不可控, 也有可能条件为空, 需要仔细检查条件内容, 防止越权操作 <pre>
     * 转换规则:
        fieldName$Equals(=), fieldName$NotEquals(!=), 
        fieldName$LessThen(<), fieldName$LessEqualsThen(<=), 
        fieldName$GreaterThen(>), fieldName$GreaterEqualsThen(>=), 
        fieldName$IsNull, fieldName$IsNotNull, 
        fieldName$Like, fieldName$NotLike, fieldName$Starts, fieldName$Ends, 
        fieldName$In, fieldName$NotIn, fieldName$Between
     * </pre>
     * 
     * @param params 请求参数
     * @param clazz 实体类
     * @param emptiable 是否允许条件为空
     * @return Where对象
     */
    public static <T> DbWhere parseParamsToDbWhere(Map<String, String[]> params, Class<T> clazz, boolean emptiable) {
        if (!emptiable) {
            VerifyTools.requireNotBlank(params, "params");
        }
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        DbWhere where = converter.parseParamsToDbWhere(params, clazz);
        if (!emptiable && where.isEmpty()) {
            throw new IllegalArgumentException("params must not be empty.");
        }
        return where;
    }

    /**
     * 从请求参数request.getParameterMap()中构建Update对象<br>
     * 只会包含clazz注解中通过@JoyInColumn指定的字段名<br>
     * 应注意, 此时参数由前端传入, 条件不可控, 也有可能条件为空, 需要仔细检查条件内容, 防止越权操作 <pre>
     * 转换规则:
        fieldName 或 fieldName$Equals(=)
        fieldName$Add(增加值)
        fieldName$ToNull(转换为空)
     * </pre>
     * 
     * @param params 请求参数
     * @param emptiable 是否允许条件为空
     * @param clazz 实体类
     * @return Update对象
     */
    public static <T> DbUpdate parseParamsToDbUpdate(Map<String, String[]> params, Class<T> clazz, boolean emptiable) {
        if (!emptiable) {
            VerifyTools.requireNotBlank(params, "params");
        }
        DbConditionConverter converter = DbTools.getDbConditionConverter();
        DbUpdate ud = converter.parseParamsToDbUpdate(params, clazz);
        if (!emptiable && ud.isEmpty()) {
            throw new IllegalArgumentException("bean must not be empty.");
        }
        return ud;
    }

}
