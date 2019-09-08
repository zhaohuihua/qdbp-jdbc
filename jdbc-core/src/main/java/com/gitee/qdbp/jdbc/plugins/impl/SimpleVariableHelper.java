package com.gitee.qdbp.jdbc.plugins.impl;

import java.sql.Types;
import java.util.Date;
import com.alibaba.fastjson.util.TypeUtils;
import com.gitee.qdbp.jdbc.model.TypedDbVariable;
import com.gitee.qdbp.jdbc.plugins.DataConvertHelper;
import com.gitee.qdbp.tools.utils.DateTools;

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
            return null;
        } else if (variable instanceof String) {
            return doStringToDbValue((String) variable);
        } else if (variable instanceof Number) {
            return doNumberToDbValue((Number) variable);
        } else if (variable instanceof Date) {
            return doDateToDbValue((Date) variable);
        } else if (variable instanceof Boolean) {
            return doBooleanToDbValue((Boolean) variable);
        } else if (variable instanceof Character) {
            return doCharacterToDbValue((Character) variable);
        } else if (variable instanceof CharSequence) {
            return doStringToDbValue(((CharSequence) variable).toString());
        } else if (variable.getClass().isEnum()) {
            return doEnumToDbValue((Enum<?>) variable);
        } else if (variable instanceof TypedDbVariable) {
            return doVariableToTypedValue((TypedDbVariable) variable);
        } else {
            return doObjectToDbValue(variable);
        }
    }

    protected Object doEnumToDbValue(Enum<?> variable) {
        return variable.ordinal();
    }

    protected Object doStringToDbValue(String variable) {
        return variable;
    }

    protected Object doNumberToDbValue(Number variable) {
        return variable;
    }

    protected Object doBooleanToDbValue(Boolean variable) {
        return variable;
    }

    protected Object doCharacterToDbValue(Character variable) {
        return variable;
    }

    protected Object doDateToDbValue(Date variable) {
        return variable;
    }

    protected Object doObjectToDbValue(Object variable) {
        return variable;
    }

    protected Object doVariableToTypedValue(TypedDbVariable variable) {
        if (variable == null) {
            return null;
        }
        Object value = variable.getValue();
        if (value == null) {
            return null;
        }
        Integer sqlType = variable.getSqlType();
        if (sqlType == null) {
            return variableToDbValue(value);
        }

        switch (sqlType) {
        case Types.BOOLEAN:
            return doVariableToBoolean(value);
        case Types.BIT:
            return doVariableToBit(value);
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER:
            return doVariableToInteger(value);
        case Types.BIGINT:
            return doVariableToLong(value);
        case Types.FLOAT:
        case Types.REAL:
        case Types.DOUBLE:
        case Types.NUMERIC:
        case Types.DECIMAL:
            return doVariableToDouble(value);
        case Types.CHAR:
        case Types.VARCHAR:
        case Types.LONGVARCHAR:
        case Types.NCHAR:
        case Types.NVARCHAR:
        case Types.LONGNVARCHAR:
            return doVariableToString(value);
        case Types.DATE:
        case Types.TIME:
        case Types.TIMESTAMP:
            return doVariableToDate(value);
        case Types.BLOB:
            return doVariableToBlob(value);
        case Types.CLOB:
        case Types.NCLOB:
            return doVariableToClob(value);
        case Types.BINARY:
            return doVariableToBinary(value);
        case Types.VARBINARY:
        case Types.LONGVARBINARY:
            return doVariableToVarBinary(value);
        case Types.NULL:
        case Types.OTHER:
        case Types.JAVA_OBJECT:
        case Types.DISTINCT:
        case Types.STRUCT:
        case Types.ARRAY:
        case Types.REF:
        case Types.DATALINK:
        case Types.ROWID:
        case Types.SQLXML:
        default:
            return variableToDbValue(value);
        }
    }

    protected String doVariableToString(Object variable) {
        return TypeUtils.castToString(variable);
    }

    protected Boolean doVariableToBoolean(Object variable) {
        return TypeUtils.castToBoolean(variable);
    }

    protected Byte doVariableToBit(Object variable) {
        return TypeUtils.castToByte(variable);
    }

    protected Integer doVariableToInteger(Object variable) {
        return TypeUtils.castToInt(variable);
    }

    protected Long doVariableToLong(Object variable) {
        return TypeUtils.castToLong(variable);
    }

    protected Double doVariableToDouble(Object variable) {
        return TypeUtils.castToDouble(variable);
    }

    protected Date doVariableToDate(Object variable) {
        if (variable instanceof CharSequence) {
            return DateTools.parse(variable.toString());
        } else {
            return TypeUtils.castToDate(variable);
        }
    }

    protected Object doVariableToBlob(Object variable) {
        return doObjectToDbValue(variable);
    }

    protected Object doVariableToBinary(Object variable) {
        return doVariableToBlob(variable);
    }

    protected Object doVariableToClob(Object variable) {
        return doVariableToString(variable);
    }

    protected Object doVariableToVarBinary(Object variable) {
        return doVariableToClob(variable);
    }

    /**
     * 将变量转换为其他的指定类型值<br>
     * 已经对常见的类型作了处理, 只有一些不常见的类型会调用该方法:<br>
     * Types.NULL, Types.JAVA_OBJECT, Types.DISTINCT, Types.STRUCT,<br>
     * Types.ARRAY, Types.REF, Types.DATALINK, Types.ROWID, Types.SQLXML, Types.OTHER
     * 
     * @param sqlType SQL数据类型({@code java.sql.Types})
     * @param value 变量值
     */
    protected Object doVariableToOtherTypedValue(int sqlType, Object variable) {
        return doObjectToDbValue(variable);
    }
}
