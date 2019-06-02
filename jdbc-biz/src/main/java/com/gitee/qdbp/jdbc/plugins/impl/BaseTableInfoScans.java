package com.gitee.qdbp.jdbc.plugins.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.jdbc.model.FieldColumn;
import com.gitee.qdbp.jdbc.model.PrimaryKey;
import com.gitee.qdbp.jdbc.plugins.TableInfoScans;

/**
 * TableAnnotationScans的一种基础实现类
 *
 * @author zhaohuihua
 * @version 190601
 */
public abstract class BaseTableInfoScans implements TableInfoScans {

    /** 扫描普通列信息 **/
    protected abstract FieldColumn scanColumn(Field field, Class<?> clazz);

    /** 扫描主键列信息, 一般情况下column为空, 只有在调用前已经扫描了column则可能column不为空 **/
    protected abstract PrimaryKey scanPrimaryKey(Field field, FieldColumn column, Class<?> clazz);

    /** 判断是否公共字段的处理器 **/
    private CommonFieldResolver commonFieldResolver;

    /** 判断是否公共字段的处理器 **/
    public CommonFieldResolver getCommonFieldResolver() {
        return commonFieldResolver;
    }

    /** 判断是否公共字段的处理器 **/
    public void setCommonFieldResolver(CommonFieldResolver commonFieldResolver) {
        this.commonFieldResolver = commonFieldResolver;
    }

    @Override
    public PrimaryKey scanPrimaryKey(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }

        Class<?> temp = clazz;
        while (temp != null && temp != Object.class) {
            Field[] fields = temp.getDeclaredFields();
            for (Field field : fields) {
                PrimaryKey pk = scanPrimaryKey(field, null, temp);
                if (pk != null) {
                    return pk;
                }
            }
            temp = temp.getSuperclass();
        }
        return null;
    }

    @Override
    public List<FieldColumn> scanColumnList(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }

        // 字段顺序: ID放在最前面, 然后按继承顺序排序, 最后放公共的字段(创建人/创建时间/更新人/更新时间/逻辑删除标记)
        FieldColumn idColumn = null;
        List<FieldColumn> commonColumns = new ArrayList<>();
        List<FieldColumn> allColumns = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        Class<?> temp = clazz;
        while (temp != null && temp != Object.class) {
            boolean isCommonPackage = isCommonPackage(temp.getPackage().getName());
            List<FieldColumn> innerCommonColumns = new ArrayList<>();
            List<FieldColumn> innerNormalColumns = new ArrayList<>();
            Field[] fields = temp.getDeclaredFields();
            for (Field field : fields) {
                if (map.containsKey(field.getName())) {
                    continue;
                }
                field.setAccessible(true);
                String fieldName = field.getName();

                FieldColumn column = scanColumn(field, temp);
                if (idColumn == null) {
                    FieldColumn tempColumn = scanPrimaryKey(field, column, temp);
                    if (tempColumn != null) {
                        idColumn = tempColumn;
                        continue; // 当前列是ID, 单独记录下来, 插入到最前面
                    }
                }
                if (column != null) {
                    if (isCommonPackage || isCommonFieldName(fieldName)) {
                        innerCommonColumns.add(column);
                    } else {
                        innerNormalColumns.add(column);
                    }
                }
            }
            if (!innerCommonColumns.isEmpty()) {
                commonColumns.addAll(0, innerCommonColumns); // 公共字段
            }
            if (!innerNormalColumns.isEmpty()) {
                allColumns.addAll(0, innerNormalColumns); // 普通字段
            }
            temp = temp.getSuperclass();
        }
        if (idColumn != null) {
            allColumns.add(0, idColumn); // ID插入到最前面
        }
        if (!commonColumns.isEmpty()) {
            allColumns.addAll(commonColumns);
        }
        return allColumns;
    }

    private boolean isCommonPackage(String pkg) {
        return commonFieldResolver != null && commonFieldResolver.isCommonPackage(pkg);
    }

    private boolean isCommonFieldName(String fieldName) {
        return commonFieldResolver != null && commonFieldResolver.isCommonFieldName(fieldName);
    }
}
