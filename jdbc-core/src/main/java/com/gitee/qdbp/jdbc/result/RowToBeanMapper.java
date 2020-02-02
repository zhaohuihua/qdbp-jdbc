package com.gitee.qdbp.jdbc.result;

import org.springframework.jdbc.core.RowMapper;

/**
 * 结果集行数据到JavaBean的转换工具
 *
 * @author zhaohuihua
 * @version 190617
 */
public interface RowToBeanMapper<T> extends RowMapper<T> {

}
