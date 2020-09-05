package com.gitee.qdbp.jdbc.plugins.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.annotations.ColumnDefault;
import com.gitee.qdbp.able.jdbc.model.DbRawValue;
import com.gitee.qdbp.jdbc.model.SimpleFieldColumn;
import com.gitee.qdbp.jdbc.plugins.CommonFieldResolver;
import com.gitee.qdbp.jdbc.plugins.TableInfoScans;
import com.gitee.qdbp.jdbc.plugins.TableNameScans;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * TableAnnotationScans的一种基础实现类
 *
 * @author zhaohuihua
 * @version 190601
 */
public abstract class BaseTableInfoScans implements TableInfoScans {

    /** 扫描列信息 **/
    protected abstract SimpleFieldColumn scanColumn(Field field, Class<?> clazz);

    /**
     * 检查是不是需要排除的字段
     * 
     * @param field 字段信息
     * @param clazz 待扫描的类
     * @return 是否排除
     */
    protected boolean isIgnoreField(Field field, Class<?> clazz) {
        // 排除transient字段
        return !Modifier.isTransient(field.getModifiers());
    }

    /** 扫描列信息之后的处理 **/
    protected void onAfterScanColumns(List<SimpleFieldColumn> allColumns) {
    }

    /** 表名扫描类 **/
    private TableNameScans tableNameScans;
    /** 判断是否公共字段的处理器 **/
    private CommonFieldResolver commonFieldResolver;

    /** 获取表名扫描类 **/
    public TableNameScans getTableNameScans() {
        return tableNameScans;
    }

    /** 设置表名扫描类 **/
    public void setTableNameScans(TableNameScans tableNameScans) {
        this.tableNameScans = tableNameScans;
    }

    /** 获取判断是否公共字段的处理器 **/
    public CommonFieldResolver getCommonFieldResolver() {
        return commonFieldResolver;
    }

    /** 设置判断是否公共字段的处理器 **/
    public void setCommonFieldResolver(CommonFieldResolver commonFieldResolver) {
        this.commonFieldResolver = commonFieldResolver;
    }

    @Override
    public String scanTableName(Class<?> clazz) {
        return getTableNameScans().scanTableName(clazz);
    }

    @Override
    public List<SimpleFieldColumn> scanColumnList(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }

        // 字段顺序: 主键放在最前面, 然后是当前类自身的字段, 然后按继承顺序依次向上取父类的字段
        // 然后是公共包下的父类字段, 以及通过字段名指定的公共字段(创建人/创建时间/更新人/更新时间/逻辑删除标记)
        SimpleFieldColumn pkColumn = null;
        List<SimpleFieldColumn> commonColumns = new ArrayList<>();
        List<SimpleFieldColumn> allColumns = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        Class<?> temp = clazz;
        while (temp != null && temp != Object.class) {
            boolean isCommonPackage = isCommonPackage(temp.getPackage().getName());
            List<SimpleFieldColumn> innerCommonColumns = new ArrayList<>();
            List<SimpleFieldColumn> innerNormalColumns = new ArrayList<>();
            Field[] fields = temp.getDeclaredFields();
            for (Field field : fields) {
                // field.setAccessible(true); // 并没有读取字段值, 不需要修改Accessible
                String fieldName = field.getName();
                // 排除父类中已被子类覆盖的字段
                if (map.containsKey(fieldName)) {
                    continue;
                }
                map.put(fieldName, null);
                // 排除静态字段
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                // 其他排除条件
                if (isIgnoreField(field, temp)) {
                    continue;
                }
                
                // 扫描列信息
                SimpleFieldColumn column = scanColumn(field, temp);
                // 判断是不是主键
                if (pkColumn == null && column.isPrimaryKey()) {
                    pkColumn = column;
                    continue; // 当前列是ID, 单独记录下来, 插入到最前面
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
        if (pkColumn != null) {
            allColumns.add(0, pkColumn); // ID插入到最前面
        }
        if (!commonColumns.isEmpty()) { // 公共字段插入到最后面
            allColumns.addAll(sortCommonColumns(commonColumns));
        }
        // 调用扫描后处理函数
        onAfterScanColumns(allColumns);
        return allColumns;
    }

    /** 扫描@ColumnDefault注解声明的默认值 **/
    protected void scanColumnDefault(Field field, SimpleFieldColumn column) {
        ColumnDefault columnDefault = field.getAnnotation(ColumnDefault.class);
        if (columnDefault != null) {
            column.setColumnDefault(parseColumnDefault(columnDefault.value()));
        }
    }

    /** 解析默认值 **/
    // @ColumnDefault("1.00");
    // @ColumnDefault("CURRENT_TIMESTAMP");
    // @ColumnDefault("'N/A'"); // 字段串要用单引号括起来
    protected Object parseColumnDefault(String defaultValue) {
        if (VerifyTools.isBlank(defaultValue)) {
            return null;
        }
        defaultValue = defaultValue.trim();
        if (defaultValue.startsWith("'") && defaultValue.endsWith("'")) {
            return defaultValue.substring(1, defaultValue.length() - 1);
        }
        if ("null".equalsIgnoreCase(defaultValue)) {
            return null;
        } else if ("true".equalsIgnoreCase(defaultValue) || "false".equalsIgnoreCase(defaultValue)) {
            // true/false在大部分的数据库都需要转换为1/0
            return new DbRawValue(defaultValue);
        } else {
            Number number = ConvertTools.toNumber(defaultValue, null);
            if (number != null) { // 是数字
                return number;
            } else { // 不是数字: CURRENT_TIMESTAMP
                return new DbRawValue(defaultValue);
            }
        }
    }

    private boolean isCommonPackage(String pkg) {
        return commonFieldResolver != null && commonFieldResolver.isCommonPackage(pkg);
    }

    private boolean isCommonFieldName(String fieldName) {
        return commonFieldResolver != null && commonFieldResolver.isCommonFieldName(fieldName);
    }

    private List<SimpleFieldColumn> sortCommonColumns(List<SimpleFieldColumn> columns) {
        if (commonFieldResolver == null) {
            return columns;
        } else {
            return commonFieldResolver.sortCommonFields(columns);
        }
    }
}
