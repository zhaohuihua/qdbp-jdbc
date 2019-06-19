package com.gitee.qdbp.able.jdbc.fields;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.gitee.qdbp.able.jdbc.utils.FieldTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 基础字段容器
 *
 * @author zhaohuihua
 * @version 180503
 */
abstract class BaseFields implements Fields, Serializable {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    protected List<String> fields;

    protected BaseFields() {
        this.fields = new ArrayList<>();
    }

    protected BaseFields(String... fields) {
        VerifyTools.requireNotBlank(fields, "fields");
        this.fields = new ArrayList<>();
        for (String field : fields) {
            VerifyTools.requireNotBlank(field, "fieldName");
            this.fields.add(field);
        }
    }

    protected BaseFields(List<String> fields) {
        VerifyTools.requireNotBlank(fields, "fields");
        this.fields = new ArrayList<>();
        this.setItems(fields);
    }

    /** 获取字段列表, 返回的是副本 **/
    public List<String> getItems() {
        List<String> list = new ArrayList<>();
        if (!this.fields.isEmpty()) {
            list.addAll(this.fields);
        }
        return list;
    }

    protected void setItems(List<String> fields) {
        for (String field : fields) {
            VerifyTools.requireNotBlank(field, "fieldName");
        }
        this.fields.clear();
        this.fields.addAll(fields);
    }

    /**
     * 判断字段名是否存在<br>
     * 判断是否匹配:<br>
     * 如果当前字段名或目标字段名没有表别名, 只要字段名匹配即为匹配<br>
     * 如果当前字段名和目标字段名都有表别名, 则需要表别名和字段名同时匹配
     * 
     * @param fieldName 字段名
     * @return 是否存在
     * @see FieldTools#matches(String, String)
     */
    protected boolean contains(String fieldName) {
        VerifyTools.requireNotBlank(fieldName, "fieldName");
        // 遍历查找匹配项
        Iterator<String> itr = this.fields.iterator();
        while (itr.hasNext()) {
            String item = itr.next();
            if (FieldTools.matches(item, fieldName)) {
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
     * @param fieldName 字段名
     * @return 字段
     * @throws IllegalArgumentException 指定的字段名存在重名字段
     * @see FieldTools#matches(String, String)
     */
    protected String find(String fieldName) throws IllegalArgumentException {
        // 遍历查找匹配项
        List<String> matched = findAll(fieldName);
        // 判断匹配项数量
        if (matched.isEmpty()) {
            return null;
        } else if (matched.size() == 1) {
            return matched.get(0);
        } else { // 不只一项则抛异常
            String desc = ConvertTools.joinToString(matched);
            throw new IllegalArgumentException("The fieldName matched to multiple field: " + desc);
        }
    }

    /**
     * 根据字段名获取指定的字段对象数组, 如果存在重名字段而fieldName未指定表别名将返回数组<br>
     * 判断是否匹配:<br>
     * 如果当前字段名或目标字段名没有表别名, 只要字段名匹配即为匹配<br>
     * 如果当前字段名和目标字段名都有表别名, 则需要表别名和字段名同时匹配
     * 
     * @param fieldName 字段名
     * @return 字段数组
     * @see FieldTools#matches(String, String)
     */
    protected List<String> findAll(String fieldName) {
        VerifyTools.requireNotBlank(fieldName, "fieldName");
        // 遍历查找匹配项
        List<String> matched = new ArrayList<>();
        Iterator<String> itr = this.fields.iterator();
        while (itr.hasNext()) {
            String item = itr.next();
            if (FieldTools.matches(item, fieldName)) {
                matched.add(item);
            }
        }
        return matched;
    }

    protected void add(String... fields) {
        VerifyTools.requireNotBlank(fields, "fields");
        for (String field : fields) {
            VerifyTools.requireNotBlank(field, "field");
            this.fields.add(field);
        }
    }

    /**
     * 删除指定字段, 如果存在重名字段而fieldName未指定表别名将抛出异常<br>
     * 判断是否匹配:<br>
     * 如果当前字段名或目标字段名没有表别名, 只要字段名匹配即为匹配<br>
     * 如果当前字段名和目标字段名都有表别名, 则需要表别名和字段名同时匹配
     * 
     * @param fieldName 待删除的字段
     * @return 是否删除了字段
     * @throws IllegalArgumentException 指定的字段名存在重名字段
     * @see FieldTools#matches(String, String)
     */
    protected boolean del(String fieldName) throws IllegalArgumentException {
        List<String> matched = this.findAll(fieldName);
        if (matched.size() == 0) {
            return false;
        } else if (matched.size() == 1) {
            this.delAll(fieldName);
            return true;
        } else {
            String desc = ConvertTools.joinToString(matched);
            throw new IllegalArgumentException("The fieldName matched to multiple field: " + desc);
        }
    }

    /**
     * 删除指定字段, 如果存在重名字段而fieldName未指定表别名将全部删除<br>
     * 判断是否匹配:<br>
     * 如果当前字段名或目标字段名没有表别名, 只要字段名匹配即为匹配<br>
     * 如果当前字段名和目标字段名都有表别名, 则需要表别名和字段名同时匹配
     * 
     * @param fieldName 待删除的字段
     * @return 删除了几个字段
     * @see FieldTools#matches(String, String)
     */
    protected int delAll(String fieldName) {
        VerifyTools.requireNotBlank(fieldName, "fieldNames");
        int count = 0;
        Iterator<String> itr = this.fields.iterator();
        while (itr.hasNext()) {
            String item = itr.next();
            if (FieldTools.matches(item, fieldName)) {
                itr.remove();
                count++;
                break;
            }
        }
        return count;
    }

}
