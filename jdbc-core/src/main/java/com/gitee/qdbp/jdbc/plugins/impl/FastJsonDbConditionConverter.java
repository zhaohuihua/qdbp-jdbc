package com.gitee.qdbp.jdbc.plugins.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.utils.FieldTools;
import com.gitee.qdbp.jdbc.model.AllFieldColumn;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * JavaBean到数据库条件的转换<br>
 * 利用fastjson进行JavaBean到Map的转换
 *
 * @author zhaohuihua
 * @version 200207
 */
public class FastJsonDbConditionConverter extends BaseDbConditionConverter {

    /** 是否清除空值 **/
    private boolean clearBlankValue = false;

    /**
     * 将Java对象转换为Map, 只保留有列信息的字段<br>
     * 先调用ParseTools.beanToMap()转换为map, 再解析列名, 只保留有列信息的字段
     * 
     * @param bean Java对象
     * @return Map对象
     */
    @Override
    public Map<String, Object> convertBeanToMap(Object bean) {
        if (bean == null) {
            return null;
        }
        Map<String, Object> map = FastJsonTools.beanToMap(bean, false, clearBlankValue);
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

    /** 是否清除空值 **/
    public boolean isClearBlankValue() {
        return clearBlankValue;
    }

    /** 是否清除空值 **/
    public void setClearBlankValue(boolean clearBlankValue) {
        this.clearBlankValue = clearBlankValue;
    }

}