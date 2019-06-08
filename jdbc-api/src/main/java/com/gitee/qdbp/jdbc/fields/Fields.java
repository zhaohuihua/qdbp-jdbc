package com.gitee.qdbp.jdbc.fields;

import java.util.List;
import com.gitee.qdbp.jdbc.model.FieldColumn;

/**
 * 字段容器
 *
 * @author zhaohuihua
 * @version 180503
 */
public interface Fields {

    List<FieldColumn> getItems();

    List<String> getFieldNames();

    List<String> getColumnNames();

}
