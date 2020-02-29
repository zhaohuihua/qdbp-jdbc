package com.gitee.qdbp.base.converter;

import org.springframework.core.convert.converter.Converter;
import com.gitee.qdbp.able.jdbc.model.DbFieldValue;
import com.gitee.qdbp.jdbc.test.enums.DataState;
import com.gitee.qdbp.tools.utils.RandomTools;

/**
 * 数据状态转换为数据库字段值<br>
 * NORMAL转换为0, DELETED转换为8位随机数
 *
 * @author zhaohuihua
 * @version 20200129
 */
public class DataStateToDbValueConverter implements Converter<DataState, DbFieldValue> {

    @Override
    public DbFieldValue convert(DataState source) {
        if (source == null || source == DataState.NORMAL) {
            return new DbFieldValue(DataState.NORMAL.ordinal());
        } else {
            return new DbFieldValue(RandomTools.generateNumber(8));
        }
    }

}