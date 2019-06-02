package com.gitee.qdbp.jdbc.fields;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import com.gitee.qdbp.jdbc.model.FieldColumn;

/**
 * 基础字段容器
 *
 * @author zhaohuihua
 * @version 180503
 */
abstract class BaseFields implements Fields, Serializable {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    protected List<FieldColumn> fields;

    protected BaseFields() {
        this.fields = new ArrayList<>();
    }

    protected BaseFields(List<FieldColumn> fields) {
        this.fields = fields;
        for (FieldColumn field : fields) {
            Objects.requireNonNull(field.getFieldName(), "fieldName");
            Objects.requireNonNull(field.getColumnName(), "columnName");
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

    public List<FieldColumn> getItems() {
        return Collections.unmodifiableList(this.fields);
    }

    public void setItems(List<FieldColumn> fields) {
        this.fields.clear();
        this.fields.addAll(fields);
    }

    protected FieldColumn get(String fieldName) {
        Objects.requireNonNull(fieldName, "fieldName");

        Iterator<FieldColumn> itr = this.fields.iterator();
        while (itr.hasNext()) {
            FieldColumn item = itr.next();
            if (fieldName.equals(item.getFieldName())) {
                return item;
            }
        }
        return null;
    }

    protected void add(FieldColumn... fields) {
        Objects.requireNonNull(fields, "fields");
        for (FieldColumn field : fields) {
            Objects.requireNonNull(field.getFieldName(), "fieldName");
            Objects.requireNonNull(field.getColumnName(), "columnName");
            this.fields.add(field);
        }
    }

    protected void add(String fieldName, String columnName) {
        this.add(fieldName, columnName, null);
    }

    protected void add(String fieldName, String columnName, String columnText) {
        Objects.requireNonNull(fieldName, "fieldName");
        Objects.requireNonNull(columnName, "columnName");
        this.fields.add(new FieldColumn(fieldName, columnName, columnText));
    }

    protected void del(String... fieldNames) {
        Objects.requireNonNull(fieldNames, "fieldNames");

        Iterator<FieldColumn> itr = this.fields.iterator();
        while (itr.hasNext()) {
            for (String fieldName : fieldNames) {
                FieldColumn item = itr.next();
                if (fieldName.equals(item.getFieldName())) {
                    itr.remove();
                    break;
                }
            }
        }
    }
}
