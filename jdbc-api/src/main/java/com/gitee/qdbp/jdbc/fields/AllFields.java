package com.gitee.qdbp.jdbc.fields;

import java.util.List;
import com.gitee.qdbp.jdbc.model.FieldColumn;
import com.gitee.qdbp.tools.utils.VerifyTools;

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
    
    public boolean isEmpty() {
        return this.fields.isEmpty();
    }

    public FilterFields include(String... fieldNames) {
        VerifyTools.requireNotBlank(fieldNames, "fieldNames");

        return new IncludeFields(this, fieldNames);
    }

    public FilterFields exclude(String... fieldNames) {
        VerifyTools.requireNotBlank(fieldNames, "fieldNames");

        return new ExcludeFields(this, fieldNames);
    }

    /** {@inheritDoc} **/
    @Override
    public boolean contains(String fieldName) {
        return super.contains(fieldName);
    }

    /** {@inheritDoc} **/
    @Override
    public FieldColumn get(String name) {
        return super.get(name);
    }

    /** {@inheritDoc} **/
    @Override
    public List<FieldColumn> getAll(String name) {
        return super.getAll(name);
    }

    /** {@inheritDoc} **/
    @Override
    public void setItems(List<FieldColumn> fields) {
        if (this.readonly) {
            throw new UnsupportedOperationException();
        }
        super.setItems(fields);
    }

    /** {@inheritDoc} **/
    @Override
    public void add(FieldColumn... fields) {
        if (this.readonly) {
            throw new UnsupportedOperationException();
        }
        super.add(fields);
    }

    /** {@inheritDoc} **/
    @Override
    public void add(String fieldName, String columnName) {
        if (this.readonly) {
            throw new UnsupportedOperationException();
        }
        super.add(fieldName, columnName);
    }

    /** {@inheritDoc} **/
    @Override
    public boolean del(String fieldName) {
        if (this.readonly) {
            throw new UnsupportedOperationException();
        }
        return super.del(fieldName);
    }

    /** {@inheritDoc} **/
    @Override
    public int delAll(String fieldName) {
        if (this.readonly) {
            throw new UnsupportedOperationException();
        }
        return super.delAll(fieldName);
    }
}
