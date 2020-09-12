package com.gitee.qdbp.jdbc.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 全部字段及列信息
 *
 * @author zhaohuihua
 * @version 190609
 */
public class AllFieldColumn<T extends SimpleFieldColumn> {

    private List<T> items;
    private Map<FieldScene, FieldColumns<T>> sceneItems;

    public AllFieldColumn(List<T> items) {
        VerifyTools.requireNotBlank(items, "items");
        List<T> list = new ArrayList<>();
        for (T item : items) {
            VerifyTools.requireNotBlank(item.getFieldName(), "fieldName");
            VerifyTools.requireNotBlank(item.getColumnName(), "columnName");
            @SuppressWarnings("unchecked")
            T copied = (T) item.copy();
            copied.setReadonly(true);
            list.add(copied);
        }
        this.items = Collections.unmodifiableList(list);
        this.sceneItems = new HashMap<>();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    /** 查找主键字段 **/
    public SimpleFieldColumn findPrimaryKey() {
        for (T item : this.items) {
            if (item.isPrimaryKey()) {
                return item;
            }
        }
        return null;
    }

    /** 根据使用场景过滤子集 **/
    public FieldColumns<T> filter(FieldScene scene) {
        if (sceneItems.containsKey(scene)) {
            return sceneItems.get(scene);
        }
        List<T> list = new ArrayList<>();
        for (T item : this.items) {
            if (sceneMatches(item, scene)) {
                list.add(item);
            }
        }
        FieldColumns<T> part = new FieldColumns<>(list);
        sceneItems.put(scene, part);
        return part;
    }

    protected boolean sceneMatches(T item, FieldScene scene) {
        if (scene == FieldScene.RESULT) {
            return true;
        }
        switch (scene) {
        case INSERT:
            return item.isColumnInsertable();
        case UPDATE:
            return item.isColumnUpdatable();
        case CONDITION:
            return item.isColumnInsertable() || item.isColumnUpdatable();
        case RESULT:
            return true;
        default:
            return item.isColumnInsertable() || item.isColumnUpdatable();
        }
    }
}
