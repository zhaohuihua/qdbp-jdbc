package com.gitee.qdbp.able.jdbc.condition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.able.jdbc.base.DbCondition;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 数据库操作容器, DbWhere/DbUpdate的父类
 *
 * @author zhaohuihua
 * @version 181221
 */
abstract class DbItems implements DbFields, Serializable {

    /** SerialVersionUID **/
    private static final long serialVersionUID = 1L;

    private List<DbCondition> items = new ArrayList<>();

    /**
     * 增加条件
     * 
     * @param fieldName 字段名
     * @param fieldValue 字段值
     */
    protected void put(String fieldName, Object fieldValue) {
        this.items.add(new DbField(fieldName, fieldValue));
    }

    /**
     * 增加条件
     * 
     * @param operateType 操作符
     * @param fieldName 字段名
     * @param fieldValue 字段值
     */
    protected void put(String operateType, String fieldName, Object fieldValue) {
        this.items.add(new DbField(operateType, fieldName, fieldValue));
    }

    /**
     * 增加条件
     * 
     * @param field 条件
     */
    protected void put(DbField field) {
        this.items.add(field);
    }

    /**
     * 增加条件
     * 
     * @param fields 容器类型的条件, 如SubWhere
     */
    protected void put(DbFields fields) {
        this.items.add(fields);
    }

    /**
     * 增加条件
     * 
     * @param condition 自定义条件
     */
    protected void put(DbCondition condition) {
        this.items.add(condition);
    }

    /** 获取内容 **/
    public List<DbCondition> items() {
        return this.items;
    }

    /** 是否为空 **/
    public boolean isEmpty() {
        if (this.items.isEmpty()) {
            return true;
        }
        Iterator<DbCondition> itr = this.items.iterator();
        while (itr.hasNext()) {
            DbCondition item = itr.next();
            if (!item.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** 清空内容 **/
    public void clear() {
        this.clear();
    }

    /**
     * 根据字段名称替换条件
     * 
     * @param field 字段
     */
    protected void replace(DbField field) {
        VerifyTools.requireNotBlank(field, "field");
        VerifyTools.requireNotBlank(field.getFieldName(), "fieldName");

        String fieldName = field.getFieldName();
        Iterator<DbCondition> itr = this.items().iterator();
        while (itr.hasNext()) {
            DbCondition item = itr.next();
            if (item instanceof DbField) {
                if (((DbField) item).matchesWithField(fieldName)) {
                    DbField target = (DbField) item;
                    target.setOperateType(field.getOperateType());
                    target.setFieldValue(field.getFieldValue());
                }
            } else if (item instanceof DbItems) {
                ((DbItems) item).replace(field);
            } else { // DbFields/DbCondition, 暂不支持替换
            }
        }
    }

    /**
     * 根据字段名称删除
     * 
     * @param fieldName 字段名称
     */
    public void remove(String fieldName) {
        VerifyTools.requireNotBlank(fieldName, "fieldName");
        Iterator<DbCondition> itr = this.items.iterator();
        while (itr.hasNext()) {
            DbCondition item = itr.next();
            if (item instanceof DbField) {
                if (((DbField) item).matchesWithField(fieldName)) {
                    itr.remove();
                }
            } else if (item instanceof DbFields) {
                ((DbFields) item).remove(fieldName);
            } else {
                if (fieldName.contains(".")) {
                    if (fieldName.equals(item.getClass().getName())) {
                        itr.remove();
                    }
                } else {
                    if (fieldName.equals(item.getClass().getSimpleName())) {
                        itr.remove();
                    }
                }
            }
        }
    }

    /** 是否存在指定的字段 **/
    public boolean contains(String fieldName) {
        VerifyTools.requireNotBlank(fieldName, "fieldName");
        Iterator<DbCondition> itr = this.items.iterator();
        while (itr.hasNext()) {
            DbCondition item = itr.next();
            if (item instanceof DbField) {
                if (((DbField) item).matchesWithField(fieldName)) {
                    return true;
                }
            } else if (item instanceof DbFields) {
                if (((DbFields) item).contains(fieldName)) {
                    return true;
                }
            } else {
                if (fieldName.contains(".")) {
                    if (fieldName.equals(item.getClass().getName())) {
                        return true;
                    }
                } else {
                    if (fieldName.equals(item.getClass().getSimpleName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 从map中获取参数构建对象
     * 
     * @param map Map参数
     * @param clazz 对象类型
     * @return 对象实例
     */
    protected static <T extends DbItems> T from(Map<String, Object> map, Class<T> clazz) {
        if (map == null) {
            throw new NullPointerException("map is null");
        }
        if (clazz == null) {
            throw new NullPointerException("clazz is null");
        }

        T items;
        try {
            items = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException("Failed to new instance for " + clazz.getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to new instance for " + clazz.getName(), e);
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (VerifyTools.isAnyBlank(key, value)) {
                continue;
            }
            int index = key.lastIndexOf('$');
            if (index < 0) {
                items.put(key, value);
            } else {
                String field = key.substring(0, index);
                String operate = key.substring(index + 1);
                items.put(operate, field, value);
            }
        }
        return items;
    }
}
