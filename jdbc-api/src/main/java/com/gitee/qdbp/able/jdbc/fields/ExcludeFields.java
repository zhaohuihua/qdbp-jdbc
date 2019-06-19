package com.gitee.qdbp.able.jdbc.fields;

import java.util.List;

/**
 * 排除型字段子集
 *
 * @author zhaohuihua
 * @version 180503
 */
class ExcludeFields extends BaseFields {

    /** 版本序列号 **/
    private static final long serialVersionUID = 1L;

    public ExcludeFields(String... fields) {
        super(fields);
    }

    public ExcludeFields(List<String> fields) {
        super(fields);
    }
}
