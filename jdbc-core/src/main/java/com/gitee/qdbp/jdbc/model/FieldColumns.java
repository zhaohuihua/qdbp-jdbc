package com.gitee.qdbp.jdbc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 字段及列信息
 *
 * @author zhaohuihua
 * @version 20200905
 */
public class FieldColumns<T extends SimpleFieldColumn> implements Iterable<T> {

    private List<T> items;

    protected FieldColumns(List<T> items) {
        VerifyTools.requireNotBlank(items, "items");
        this.items = Collections.unmodifiableList(items);
    }

    /** 获取字段对象列表 **/
    public List<T> items() {
        return items;
    }

    @Override
    public Iterator<T> iterator() {
        return items.iterator();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public int size() {
        return this.items.size();
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
     * @see SimpleFieldColumn#matchesByFieldName(String)
     * @see TablesFieldColumn#matchesByFieldName(String)
     */
    public boolean containsByFieldName(String fieldName) {
        VerifyTools.requireNotBlank(fieldName, "fieldName");
        // 遍历查找匹配项
        Iterator<T> itr = this.items.iterator();
        while (itr.hasNext()) {
            T item = itr.next();
            if (item.matchesByFieldName(fieldName)) {
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
     * @see SimpleFieldColumn#matchesByFieldName(String)
     * @see TablesFieldColumn#matchesByFieldName(String)
     */
    public T findByFieldName(String fieldName) throws IllegalArgumentException {
        // 遍历查找匹配项
        List<T> matched = findAllByFieldName(fieldName);
        // 判断匹配项数量
        if (matched.isEmpty()) {
            return null;
        } else if (matched.size() == 1) {
            T field = matched.get(0);
            return field;
        } else { // 不只一项则抛异常
            String desc = toFieldDescString(matched);
            throw new IllegalArgumentException("The fieldName matched to multiple field: " + desc);
        }
    }

    /**
     * 根据字段名获取指定的字段对象数组(如a.userId,b.userId)<br>
     * 判断是否匹配:<br>
     * 如果当前字段名或目标字段名没有表别名, 只要字段名匹配即为匹配<br>
     * 如果当前字段名和目标字段名都有表别名, 则需要表别名和字段名同时匹配
     * 
     * @param fieldName 目标字段名
     * @return 字段对象数组, 返回的是对象副本
     * @see SimpleFieldColumn#matchesByFieldName(String)
     * @see TablesFieldColumn#matchesByFieldName(String)
     */
    public List<T> findAllByFieldName(String fieldName) {
        VerifyTools.requireNotBlank(fieldName, "fieldName");
        // 遍历查找匹配项
        List<T> matched = new ArrayList<>();
        Iterator<T> itr = this.items.iterator();
        while (itr.hasNext()) {
            T item = itr.next();
            if (item.matchesByFieldName(fieldName)) {
                matched.add(item);
            }
        }
        return matched;
    }

    /**
     * 根据列名获取指定的字段对象, 如果存在重名字段而columnName未指定表别名将抛出异常<br>
     * 判断是否匹配:<br>
     * 如果当前列名或目标列名没有表别名, 只要列名匹配即为匹配<br>
     * 如果当前列名和目标列名都有表别名, 则需要表别名和列名同时匹配
     * 
     * @param columnName 目标列名
     * @return 字段对象, 返回的是对象副本
     * @throws IllegalArgumentException 指定的列名存在重名字段
     * @see SimpleColumnColumn#matchesByColumn(String)
     * @see TablesColumnColumn#matchesByColumn(String)
     */
    public T findByColumnName(String columnName) {
        // 遍历查找匹配项
        List<T> matched = findAllByColumnName(columnName);
        // 判断匹配项数量
        if (matched.isEmpty()) {
            return null;
        } else if (matched.size() == 1) {
            return matched.get(0);
        } else { // 不只一项则抛异常
            String desc = toFieldDescString(matched);
            throw new IllegalArgumentException("The columnName matched to multiple column: " + desc);
        }
    }

    /**
     * 根据列名获取指定的字段对象(如A.USER_ID,B.USER_ID)<br>
     * 判断是否匹配:<br>
     * 如果当前列名或目标列名没有表别名, 只要列名匹配即为匹配<br>
     * 如果当前列名和目标列名都有表别名, 则需要表别名和列名同时匹配
     * 
     * @param columnName 目标列名
     * @return 字段对象, 返回的是对象副本
     * @see SimpleColumnColumn#matchesByColumnName(String)
     * @see TablesColumnColumn#matchesByColumnName(String)
     */
    public List<T> findAllByColumnName(String columnName) {
        VerifyTools.requireNotBlank(columnName, "columnName");
        // 遍历查找匹配项
        List<T> matched = new ArrayList<>();
        Iterator<T> itr = this.items.iterator();
        while (itr.hasNext()) {
            T item = itr.next();
            if (item.matchesByColumnName(columnName)) {
                matched.add(item);
            }
        }
        return matched;
    }

    /**
     * 根据列别名获取指定的字段对象(用于根据结果集的列别名查询字段信息)<br>
     * 如果没有columnAlias将与columnName比较
     * 
     * @param columnAlias 目标列别名
     * @return 字段对象, 返回的是对象副本
     * @see SimpleColumnColumn#matchesByColumnAlias(String)
     * @see TablesColumnColumn#matchesByColumnAlias(String)
     */
    public T findByColumnAlias(String columnAlias) {
        VerifyTools.requireNotBlank(columnAlias, "columnAlias");
        // 遍历查找匹配项
        Iterator<T> itr = this.items.iterator();
        while (itr.hasNext()) {
            T item = itr.next();
            if (item.matchesByColumnAlias(columnAlias)) {
                return item;
            }
        }
        return null;
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
