package com.gitee.qdbp.jdbc.fields;

import java.util.List;
import java.util.Objects;
import com.gitee.qdbp.jdbc.model.FieldColumn;

/**
 * 全字段容器
 *
 * @author zhaohuihua
 * @version 180503
 */
public class AllFields extends BaseFields {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    private boolean readonly;

    public AllFields() {
        super();
        this.readonly = false;
    }

    public AllFields(List<FieldColumn> fields) {
        super(fields);
        this.readonly = false;
    }

    public void readonly() {
        this.readonly = true;
    }

    public FilterFields include(String... fieldNames) {
        Objects.requireNonNull(fieldNames, "fieldNames");

        return new IncludeFields(this, fieldNames);
    }

    public FilterFields exclude(String... fieldNames) {
        Objects.requireNonNull(fieldNames, "fieldNames");

        return new ExcludeFields(this, fieldNames);
    }

    @Override
    public FieldColumn get(String name) {
        return super.get(name);
    }

    @Override
    public void setItems(List<FieldColumn> fields) {
        if (this.readonly) {
            throw new UnsupportedOperationException();
        }
        super.setItems(fields);
    }

    @Override
    public void add(FieldColumn... fields) {
        if (this.readonly) {
            throw new UnsupportedOperationException();
        }
        super.add(fields);
    }

    @Override
    public void add(String fieldName, String columnName, String columnText) {
        if (this.readonly) {
            throw new UnsupportedOperationException();
        }
        super.add(fieldName, columnName, columnText);
    }

    @Override
    public void del(String... fieldNames) {
        if (this.readonly) {
            throw new UnsupportedOperationException();
        }
        super.del(fieldNames);
    }
}
