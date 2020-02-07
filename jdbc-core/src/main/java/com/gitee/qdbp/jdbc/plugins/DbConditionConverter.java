package com.gitee.qdbp.jdbc.plugins;

import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;

/**
 * JavaBean到数据库条件的转换
 *
 * @author zhaohuihua
 * @version 200207
 */
public interface DbConditionConverter {

    /**
     * 将Java对象转换为Insert操作的Map参数
     * 
     * @param bean Java对象
     * @return Map
     */
    Map<String, Object> convertBeanToInsertMap(Object bean);

    /**
     * 将Java对象转换为DbWhere条件
     * 
     * @param bean Java对象
     * @return DbWhere
     */
    DbWhere convertBeanToDbWhere(Object bean);

    /**
     * 将Java对象转换为DbUpdate对象
     * 
     * @param bean Java对象
     * @return DbUpdate
     */
    DbUpdate convertBeanToDbUpdate(Object bean);

    /**
     * 将请求参数转换为DbWhere对象
     * 
     * @param params 请求参数
     * @return DbWhere
     */
    <T> DbWhere parseParamsToDbWhere(Map<String, String[]> params, Class<T> clazz);

    /**
     * 将请求参数转换为DbUpdate对象
     * 
     * @param params 请求参数
     * @return DbUpdate
     */
    <T> DbUpdate parseParamsToDbUpdate(Map<String, String[]> params, Class<T> clazz);
}
