package com.gitee.qdbp.jdbc.plugins;

import java.util.Map;
import com.gitee.qdbp.able.jdbc.condition.DbUpdate;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;

/**
 * JavaBean到数据库条件的转换
 *
 * @author zhaohuihua
 * @version 20200207
 */
public interface DbConditionConverter {

    /**
     * 将Java对象转换为Insert操作的Map参数<br>
     * <b>注意:</b> 值为null的字段将被忽略; 值为空字符串的字段, 表示用户未填写, <b>也会被忽略</b><br>
     * 因为这里的bean对象极有可能来自于controller的参数(最终来自于用户填写的表单)<br>
     * 如果不忽略空字符串, 考虑field1=value1,field2=""的场景<br>
     * 生成的SQL语句就会是这样的INSERT INTO table(field1,field2) VALUES(value1,NULL)<br>
     * 这就会导致field2无法使用数据库设置的默认值
     * 
     * @param bean Java对象
     * @return Map参数
     */
    Map<String, Object> convertBeanToInsertMap(Object bean);

    /**
     * 将Java对象转换为Update操作的Map参数<br>
     * <b>注意:</b> 值为null的字段将被忽略;<br>
     * 值为空字符串的字段将被保留(表示设置为NULL), 此特性与insert/where不同<br>
     * 因为这里的bean对象极有可能来自于controller的参数(最终来自于用户填写的表单)<br>
     * 如果忽略空字符串, 用户想要将已经有值的字段设置为NULL将变得难以处理
     * 
     * @param bean Java对象
     * @return Map参数
     */
    Map<String, Object> convertBeanToUpdateMap(Object bean);

    /**
     * 将Java对象转换为DbUpdate对象<br>
     * <b>注意:</b> 值为null的字段将被忽略;<br>
     * 值为空字符串的字段将被转换为SET field=NULL(见DbBinarySetOperator), 此特性与insert/where不同<br>
     * 因为这里的bean对象极有可能来自于controller的参数(最终来自于用户填写的表单)<br>
     * 如果忽略空字符串, 用户想要将已经有值的字段设置为NULL将变得难以处理
     * 
     * @param bean Java对象
     * @return DbUpdate对象
     */
    DbUpdate convertBeanToDbUpdate(Object bean);

    /**
     * 将Java对象转换为DbWhere条件<br>
     * <b>注意:</b> 值为null的字段将被忽略;<br>
     * 值为空字符串的字段, 表示用户未填写, <b>也会被忽略</b><br>
     * 因为这里的bean对象极有可能来自于controller的参数(最终来自于用户填写的表单)<br>
     * 如果不忽略空字符串, 生成的SQL语句就会带有很多的FIELD1 IS NULL and FIELD2 IS NULL这样的条件
     * 
     * @param bean Java对象
     * @return DbWhere条件
     */
    DbWhere convertBeanToDbWhere(Object bean);

    /**
     * 从map中获取参数构建DbWhere对象<br>
     * <b>注意:</b> 值为null的字段将被忽略; 值为空字符串的字段, <b>也会被忽略</b><br>
     * 由于参数是map, 如果要查询IS NULL条件, 可以使用map.put("field$IsNull", true)这种方式<br>
     * map参数比较自由, 除了支持map.put("field", value)这种表示等于的条件<br>
     * 还支持field$IsNull, field$NotEquals, field$GreaterThen, field$GreaterEquals...等<br>
     * 还支持map.put("field$In", Arrays.asList(value1, value2); 表示WHERE field IN (value1,value2)<br>
     * 还支持map.put("field$GreaterThen", new DbFieldName("field2")); 表示WHERE field>field2<br>
     * 
     * @param map Map参数
     * @return DbWhere对象实例
     */
    DbWhere parseMapToDbWhere(Map<String, Object> map);

    /**
     * 从map中获取参数构建DbUpdate对象<br>
     * <b>注意:</b> 值为null的字段将被忽略;<br>
     * 值为空字符串的字段将被转换为SET field=NULL(见DbBinarySetOperator), 此特性与insert/where不同<br>
     * map参数比较自由, 除了支持map.put("field", value)这种表示SET field=value的条件<br>
     * 还支持map.put("field$ToNull", true); 表示SET field=NULL<br>
     * 还支持map.put("field$Add", number); 表示SET field=field+number<br>
     * 还支持map.put("field$Add", new DbFieldName("field2")); 表示SET field=field+field2<br>
     * 
     * @param map Map参数
     * @return DbUpdate对象实例
     */
    DbUpdate parseMapToDbUpdate(Map<String, Object> map);

    /**
     * 将请求参数转换为DbWhere条件
     * 
     * @param params 请求参数
     * @param beanType JavaBean类型
     * @return DbWhere条件
     * @see #parseMapToDbWhere(Map) 详见parseMapToDbWhere(Map)说明
     */
    DbWhere parseParamsToDbWhere(Map<String, String[]> params, Class<?> beanType);

    /**
     * 将请求参数转换为DbUpdate对象
     * 
     * @param params 请求参数
     * @param beanType JavaBean类型
     * @return DbUpdate对象
     * @see #parseMapToDbUpdate(Map) 详见parseMapToDbUpdate(Map)说明
     */
    DbUpdate parseParamsToDbUpdate(Map<String, String[]> params, Class<?> beanType);
}
