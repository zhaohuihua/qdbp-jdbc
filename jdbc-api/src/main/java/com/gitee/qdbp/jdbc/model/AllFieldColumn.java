package com.gitee.qdbp.jdbc.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 全部字段及列信息
 *
 * @author zhaohuihua
 * @version 190609
 */
public class AllFieldColumn<T extends SimpleFieldColumn> {

    private List<T> items;

    public AllFieldColumn(List<T> items) {
        for (T field : items) {
            VerifyTools.requireNotBlank(field.getFieldName(), "fieldName");
            VerifyTools.requireNotBlank(field.getColumnName(), "columnName");
        }
        this.items = new ArrayList<>();
        this.items.addAll(items);
    }

    /** 获取字段对象列表, 返回的是对象副本 **/
    public List<T> items() {
        List<T> list = new ArrayList<>();
        for (T field : this.items) {
            @SuppressWarnings("unchecked")
            T copied = (T) field.to(field.getClass());
            list.add(copied);
        }
        return list;
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    /**
     * 获取字段名列表
     * 
     * @return 字段名列表
     */
    public List<String> getFieldNames() {
        List<String> list = new ArrayList<>();
        for (T item : this.items) {
            list.add(item.toTableFieldName());
        }
        return list;
    }

    /**
     * 获取列名列表
     * 
     * @return 列名列表
     */
    public List<String> getColumnNames() {
        List<String> list = new ArrayList<>();
        for (T item : this.items) {
            list.add(item.toTableColumnName());
        }
        return list;
    }

    /**
     * 判断字段名是否存在<br>
     * 判断是否匹配:<br>
     * 如果当前字段名或目标字段名没有表别名, 只要字段名匹配即为匹配<br>
     * 如果当前字段名和目标字段名都有表别名, 则需要表别名和字段名同时匹配
     * 
     * @param fieldName 目标字段名
     * @return 是否存在
     * @see SimpleFieldColumn#matchesByField(String)
     * @see TablesFieldColumn#matchesByField(String)
     */
    public boolean containsByField(String fieldName) {
        VerifyTools.requireNotBlank(fieldName, "fieldName");
        // 遍历查找匹配项
        Iterator<T> itr = this.items.iterator();
        while (itr.hasNext()) {
            T item = itr.next();
            if (item.matchesByField(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据字段名获取指定的字段对象, 如果存在重名字段而fieldName未指定表别名将抛出异常<br>
     * 判断是否匹配:<br>
     * 如果当前字段名或目标字段名没有表别名, 只要字段名匹配即为匹配<br>
     * 如果当前字段名和目标字段名都有表别名, 则需要表别名和字段名同时匹配
     * 
     * @param fieldName 目标字段名
     * @return 字段对象, 返回的是对象副本
     * @throws IllegalArgumentException 指定的字段名存在重名字段
     * @see SimpleFieldColumn#matchesByField(String)
     * @see TablesFieldColumn#matchesByField(String)
     */
    public T findByField(String fieldName) throws IllegalArgumentException {
        // 遍历查找匹配项
        List<T> matched = findAllByField(fieldName);
        // 判断匹配项数量
        if (matched.isEmpty()) {
            return null;
        } else if (matched.size() == 1) {
            T field = matched.get(0);
            @SuppressWarnings("unchecked")
            T copied = (T) field.to(field.getClass());
            return copied;
        } else { // 不只一项则抛异常
            String desc = toFieldDescString(matched);
            throw new IllegalArgumentException("The fieldName matched to multiple field: " + desc);
        }
    }

    /**
     * 根据字段名获取指定的字段对象数组, 如果存在重名字段而fieldName未指定表别名将返回数组<br>
     * 判断是否匹配:<br>
     * 如果当前字段名或目标字段名没有表别名, 只要字段名匹配即为匹配<br>
     * 如果当前字段名和目标字段名都有表别名, 则需要表别名和字段名同时匹配
     * 
     * @param fieldName 目标字段名
     * @return 字段对象数组, 返回的是对象副本
     * @see SimpleFieldColumn#matchesByField(String)
     * @see TablesFieldColumn#matchesByField(String)
     */
    public List<T> findAllByField(String fieldName) {
        VerifyTools.requireNotBlank(fieldName, "fieldName");
        // 遍历查找匹配项
        List<T> matched = new ArrayList<>();
        Iterator<T> itr = this.items.iterator();
        while (itr.hasNext()) {
            T item = itr.next();
            if (item.matchesByField(fieldName)) {
                matched.add(item);
            }
        }
        return matched;
    }

    private String toFieldDescString(List<T> matched) {
        StringBuilder buffer = new StringBuilder();
        for (T field : matched) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(field.toTableFieldName());
        }
        return buffer.toString();
    }

}
