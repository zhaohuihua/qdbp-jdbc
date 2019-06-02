package com.gitee.qdbp.jdbc.fields;

import java.util.List;
import java.util.Objects;
import com.gitee.qdbp.jdbc.model.FieldColumn;

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

    public FilterFields include(String... fieldNames) {
        Objects.requireNonNull(fieldNames, "fieldNames");
        for (String name : fieldNames) {
            FieldColumn item = this.all.get(name);
            if (item == null) {
                throw new IllegalArgumentException("Field '" + name + "' not exists.");
            } else {
                super.add(item);
            }
        }
        return this;
    }

    public FilterFields exclude(String... names) {
        super.del(names);
        return this;
    }
}
