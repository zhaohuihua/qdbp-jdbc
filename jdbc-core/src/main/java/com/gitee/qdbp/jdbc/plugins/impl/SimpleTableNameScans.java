package com.gitee.qdbp.jdbc.plugins.impl;

import javax.persistence.Table;
import com.gitee.qdbp.jdbc.plugins.NameConverter;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 默认的表名扫描类
 *
 * @author zhaohuihua
 * @version 190727
 */
public class SimpleTableNameScans extends BaseTableNameScans {

    @Override
    public String scanTableName(Class<?> clazz) {
        Table annotation = clazz.getAnnotation(Table.class);
        if (annotation != null && VerifyTools.isNotBlank(annotation.name())) {
            return annotation.name();
        } else {
            NameConverter nameConverter = this.getNameConverter();
            return nameConverter.beanNameToTableName(clazz.getSimpleName());
        }
    }
}
