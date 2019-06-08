package com.gitee.qdbp.jdbc.fields;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.gitee.qdbp.jdbc.model.FieldColumn;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 基础字段容器
 *
 * @author zhaohuihua
 * @version 180503
 */
abstract class BaseFields implements Fields, Iterable<FieldColumn>, Serializable {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    protected List<FieldColumn> fields;

    protected BaseFields() {
        this.fields = new ArrayList<>();
    }

    protected BaseFields(List<FieldColumn> fields) {
        this.fields = fields;
        for (FieldColumn field : fields) {
            VerifyTools.requireNotBlank(field.getFieldName(), "fieldName");
            VerifyTools.requireNotBlank(field.getColumnName(), "columnName");
        }
    }

    @Override
    public List<String> getFieldNames() {
        List<String> list = new ArrayList<>();
        for (FieldColumn field : this.fields) {
            list.add(field.getFieldName());
        }
        return list;
    }

    @Override
    public List<String> getColumnNames() {
        List<String> list = new ArrayList<>();
        for (FieldColumn field : this.fields) {
            list.add(field.getColumnName());
        }
        return list;
    }

    /** 获取字段对象列表, 返回的是对象副本 **/
    public List<FieldColumn> getItems() {
        List<FieldColumn> list = new ArrayList<>();
        for (FieldColumn field : this.fields) {
            list.add(field.to(FieldColumn.class));
        }
        return list;
    }

    public void setItems(List<FieldColumn> fields) {
        this.fields.clear();
        this.fields.addAll(fields);
    }

    public Iterator<FieldColumn> iterator() {
        return this.fields.iterator();
    }

    /**
     * 根据字段名获取指定的字段对象, 如果存在重名字段而fieldName未指定表别名将抛出异常<br>
     * 判断是否匹配:<br>
     * 如果未指定表别名或FieldColumn没有表别名, 只要字段名匹配即为匹配<br>
     * 如果指定了表别名且FieldColumn有表别名, 则需要表别名和字段名同时匹配<br>
     * 
     * @param fieldName 字段名
     * @return 字段对象, 返回的是对象副本
     * @throws IllegalArgumentException 指定的字段名存在重名字段
     * @see FieldColumn#matchesWithField(String)
     */
    protected FieldColumn get(String fieldName) throws IllegalArgumentException {
        // 遍历查找匹配项
        List<FieldColumn> matched = getAll(fieldName);
        // 判断匹配项数量
        if (matched.isEmpty()) {
            return null;
        } else if (matched.size() == 1) {
            return matched.get(0).to(FieldColumn.class);
        } else { // 不只一项则抛异常
            String desc = toFieldDescString(matched);
            throw new IllegalArgumentException("The fieldName matched to multiple field: " + desc);
        }
    }

    /**
     * 根据字段名获取指定的字段对象数组, 如果存在重名字段而fieldName未指定表别名将返回数组<br>
     * 判断是否匹配:<br>
     * 如果未指定表别名或FieldColumn没有表别名, 只要字段名匹配即为匹配<br>
     * 如果指定了表别名且FieldColumn有表别名, 则需要表别名和字段名同时匹配<br>
     * 
     * @param fieldName 字段名
     * @return 字段对象数组, 返回的是对象副本
     * @see FieldColumn#matchesWithField(String)
     */
    protected List<FieldColumn> getAll(String fieldName) {
        VerifyTools.requireNotBlank(fieldName, "fieldName");
        // 遍历查找匹配项
        List<FieldColumn> matched = new ArrayList<>();
        Iterator<FieldColumn> itr = this.fields.iterator();
        while (itr.hasNext()) {
            FieldColumn item = itr.next();
            if (item.matchesWithField(fieldName)) {
                matched.add(item);
            }
        }
        return matched;
    }

    protected void add(FieldColumn... fields) {
        VerifyTools.requireNotBlank(fields, "fields");
        for (FieldColumn field : fields) {
            VerifyTools.requireNotBlank(field, "field");
            this.fields.add(field);
        }
    }

    protected void add(String fieldName, String columnName) {
        this.add(new FieldColumn(fieldName, columnName));
    }

    /**
     * 删除指定字段, 如果存在重名字段而fieldName未指定表别名将抛出异常<br>
     * 判断是否匹配:<br>
     * 如果未指定表别名或FieldColumn没有表别名, 只要字段名匹配即为匹配<br>
     * 如果指定了表别名且FieldColumn有表别名, 则需要表别名和字段名同时匹配<br>
     * 
     * @param fieldName 待删除的字段
     * @return 是否删除了字段
     * @throws IllegalArgumentException 指定的字段名存在重名字段
     * @see FieldColumn#matchesWithField(String)
     */
    protected boolean del(String fieldName) throws IllegalArgumentException {
        List<FieldColumn> matched = this.getAll(fieldName);
        if (matched.size() == 0) {
            return false;
        } else if (matched.size() == 1) {
            this.delAll(fieldName);
            return true;
        } else {
            String desc = toFieldDescString(matched);
            throw new IllegalArgumentException("The fieldName matched to multiple field: " + desc);
        }
    }

    /**
     * 删除指定字段, 如果存在重名字段而fieldName未指定表别名将全部删除<br>
     * 判断是否匹配:<br>
     * 如果未指定表别名或FieldColumn没有表别名, 只要字段名匹配即为匹配<br>
     * 如果指定了表别名且FieldColumn有表别名, 则需要表别名和字段名同时匹配<br>
     * 
     * @param fieldName 待删除的字段
     * @return 删除了几个字段
     * @see FieldColumn#matchesWithField(String)
     */
    protected int delAll(String fieldName) {
        VerifyTools.requireNotBlank(fieldName, "fieldNames");
        int count = 0;
        Iterator<FieldColumn> itr = this.fields.iterator();
        while (itr.hasNext()) {
            FieldColumn item = itr.next();
            if (item.matchesWithField(fieldName)) {
                itr.remove();
                count++;
                break;
            }
        }
        return count;
    }

    private String toFieldDescString(List<FieldColumn> matched) {
        StringBuilder buffer = new StringBuilder();
        for (FieldColumn field : matched) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            if (VerifyTools.isBlank(field.getTableAlias())) {
                buffer.append(field.getFieldName());
            } else {
                buffer.append(field.getTableAlias()).append('.').append(field.getFieldName());
            }
        }
        return buffer.toString();
    }
}
