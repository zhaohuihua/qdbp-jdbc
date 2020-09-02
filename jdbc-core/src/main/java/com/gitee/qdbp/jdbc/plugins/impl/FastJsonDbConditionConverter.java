package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.utils.FieldTools;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.plugins.BeanToMapConverter;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * JavaBean到数据库条件的转换<br>
 * 利用fastjson进行JavaBean到Map的转换
 *
 * @author zhaohuihua
 * @version 20200207
 */
public class FastJsonDbConditionConverter extends BaseDbConditionConverter {

    /**
     * 将Java对象转换为Map, 只保留有列信息的字段<br>
     * 先调用ParseTools.beanToMap()转换为map, 再解析列名, 只保留有列信息的字段
     * 
     * @param bean Java对象
     * @return Map对象
     */
    @Override
    protected Map<String, Object> convertBeanToDbMap(Object bean) {
        if (bean == null) {
            return null;
        }
        BeanToMapConverter beanToMapConverter = DbTools.getBeanToMapConverter();
        // deep=false: 不需要递归转换; 字段是实体类的不需要转换
        // clearBlankValue=false: 不需要清理空值; 因为在Update时, Null值和空字符串代表不同的含义
        Map<String, Object> map = beanToMapConverter.convert(bean, false, false);
        if (VerifyTools.isBlank(map)) {
            return map;
        }

        // 从bean.getClass()扫描获取列名与字段名的对应关系
        AllFieldColumn<?> allFields = DbTools.parseToAllFieldColumn(bean.getClass());
        if (allFields == null || allFields.isEmpty()) {
            return null;
        }
        List<String> fieldNames = allFields.getFieldNames();

        // 只保留有列信息的字段
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (FieldTools.contains(fieldNames, entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

}
