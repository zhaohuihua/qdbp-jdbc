package com.gitee.qdbp.jdbc.fields;

import java.util.List;
import com.gitee.qdbp.jdbc.model.FieldColumn;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 过滤字段容器, 在全字段的基础上, 通过include导入或exclude排除, 得到字段子集
 *
 * @author zhaohuihua
 * @version 180503
 */
public abstract class FilterFields extends BaseFields {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    protected AllFields all;

    protected FilterFields(AllFields all) {
        super();
        this.all = all;
    }

    protected FilterFields(AllFields all, List<FieldColumn> fields) {
        super(fields);
        this.all = all;
    }

    /**
     * 在全字段的基础上, 通过include导入指定字段, 得到字段子集; 如果存在重名字段而fieldName未指定表别名将抛出异常<br>
     * 判断是否匹配:<br>
     * 如果未指定表别名或FieldColumn没有表别名, 只要字段名匹配即为匹配<br>
     * 如果指定了表别名且FieldColumn有表别名, 则需要表别名和字段名同时匹配<br>
     * 
     * @param fieldName 字段名
     * @return 字段对象, 返回的是对象副本
     * @throws IllegalArgumentException 指定的字段名不存在, 或指定的字段名存在重名字段
     * @see FieldColumn#matchesWithField(String)
     */
    public FilterFields include(String... fieldNames) {
        VerifyTools.requireNotBlank(fieldNames, "fieldNames");
        // 检查
        for (String fieldName : fieldNames) {
            FieldColumn item = this.all.get(fieldName);
            if (item == null) {
                throw new IllegalArgumentException("Field '" + fieldName + "' not exists.");
            }
        }
        // 按原始顺序导入
        for (FieldColumn item : this.all) {
            for (String fieldName : fieldNames) {
                if (item.matchesWithField(fieldName)) {
                    super.add(item);
                    break;
                }
            }
        }
        return this;
    }

    /**
     * 在全字段的基础上, 通过exclude排除指定字段, 得到字段子集; 如果存在重名字段而fieldName未指定表别名将抛出异常<br>
     * 判断是否匹配:<br>
     * 如果未指定表别名或FieldColumn没有表别名, 只要字段名匹配即为匹配<br>
     * 如果指定了表别名且FieldColumn有表别名, 则需要表别名和字段名同时匹配<br>
     * 
     * @param fieldName 字段名
     * @return 字段对象, 返回的是对象副本
     * @throws IllegalArgumentException 指定的字段名不存在, 或指定的字段名存在重名字段
     * @see FieldColumn#matchesWithField(String)
     */
    public FilterFields exclude(String... fieldNames) {
        VerifyTools.requireNotBlank(fieldNames, "fieldNames");
        for (String fieldName : fieldNames) {
            boolean succ = super.del(fieldName);
            if (!succ) {
                throw new IllegalArgumentException("Field '" + fieldName + "' not exists.");
            }
        }
        return this;
    }
}
