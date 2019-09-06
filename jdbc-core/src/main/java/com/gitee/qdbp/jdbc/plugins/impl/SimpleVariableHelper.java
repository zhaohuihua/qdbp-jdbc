package com.gitee.qdbp.jdbc.plugins.impl;

import java.sql.Types;
import java.util.Date;
import com.alibaba.fastjson.util.TypeUtils;
import com.gitee.qdbp.jdbc.model.VariableValue;
import com.gitee.qdbp.jdbc.plugins.DataConvertHelper;
import com.gitee.qdbp.tools.utils.DateTools;
import com.gitee.qdbp.tools.utils.StringTools;

/**
 * VariableHelper简单实现类
 *
 * @author zhaohuihua
 * @version 190705
 */
public class SimpleVariableHelper implements DataConvertHelper {

    @Override
    public Object variableToDbValue(Object variable) {
        if (variable == null) {
            return variable;
        } else if (variable instanceof String) {
            return doVariableToDbValue((String) variable);
        } else if (variable instanceof Number) {
            return doVariableToDbValue((Number) variable);
        } else if (variable instanceof Date) {
            return doVariableToDbValue((Date) variable);
        } else if (variable instanceof Boolean) {
            return doVariableToDbValue((Boolean) variable);
        } else if (variable instanceof Character) {
            return doVariableToDbValue((Character) variable);
        } else if (variable instanceof CharSequence) {
            return doVariableToDbValue((CharSequence) variable);
        } else if (variable.getClass().isEnum()) {
            return doVariableToDbValue((Enum<?>) variable);
        } else {
            return doVariableToDbValue(variable);
        }
    }

    protected Object doVariableToDbValue(Enum<?> variable) {
        return variable.ordinal();
    }

    protected Object doVariableToDbValue(String variable) {
        return variable;
    }

    protected Object doVariableToDbValue(CharSequence variable) {
        return variable.toString();
    }

    protected Object doVariableToDbValue(Number variable) {
        return variable;
    }

    protected Object doVariableToDbValue(Boolean variable) {
        return variable;
    }

    protected Object doVariableToDbValue(Character variable) {
        return variable;
    }

    protected Object doVariableToDbValue(Date variable) {
        return variable;
    }

    protected Object doVariableToDbValue(Object variable) {
        return variable;
    }

    protected Object doVariableToDbValue(VariableValue variable) {
        Object value = variable.getValue();
        if (value == null) {
            return null;
        }
        Integer sqlType = variable.getSqlType();
        if (sqlType == null) {
            return doVariableToDbValue(value);
        }

        // TODO 转换为指定类型
        switch (sqlType) {
        case Types.BIT:
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
        case Types.BIGINT:
        case Types.FLOAT:
        case Types.REAL:
        case Types.DOUBLE:
        case Types.NUMERIC:
        case Types.DECIMAL:
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.DATE:
        case Types.TIME:
        case Types.TIMESTAMP:
        case Types.BINARY:
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
        case Types.NULL:
        case Types.OTHER:
        case Types.JAVA_OBJECT:
        case Types.DISTINCT:
        case Types.STRUCT:
        case Types.ARRAY:
        case Types.BLOB:
        case Types.CLOB:
        case Types.REF:
        case Types.DATALINK:
        case Types.BOOLEAN:
        case Types.ROWID:
        case Types.NCHAR:
        case Types.NVARCHAR:
        case Types.LONGNVARCHAR:
        case Types.NCLOB:
        case Types.SQLXML:
        }
        return doVariableToDbValue(value);
    }

    protected String doVariableToString(Object variable) {
        return TypeUtils.castToString(variable);
    }

    protected Boolean doVariableToBoolean(Object variable) {
        if (variable instanceof CharSequence) {
            return StringTools.isPositive(variable.toString(), false);
        } else {
            return TypeUtils.castToBoolean(variable);
        }
    }

    protected Double doVariableToDouble(Object variable) {
        return TypeUtils.castToDouble(variable);
    }

    protected Long doVariableToLong(Object variable) {
        return TypeUtils.castToLong(variable);
    }

    protected Date doVariableToDate(Object variable) {
        if (variable instanceof CharSequence) {
            return DateTools.parse((String) variable);
        } else {
            return TypeUtils.castToDate(variable);
        }
    }
}
