package com.gitee.qdbp.jdbc.sql.fragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.gitee.qdbp.able.model.db.DbCondition;
import com.gitee.qdbp.able.model.db.WhereCondition;
import com.gitee.qdbp.able.model.ordering.OrderType;
import com.gitee.qdbp.able.model.ordering.Ordering;
import com.gitee.qdbp.jdbc.condition.DbField;
import com.gitee.qdbp.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.condition.SubWhere;
import com.gitee.qdbp.jdbc.condition.TableJoin;
import com.gitee.qdbp.jdbc.exception.UnsupportedFieldExeption;
import com.gitee.qdbp.jdbc.model.FieldColumn;
import com.gitee.qdbp.jdbc.model.PrimaryKey;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.plugins.WhereSqlBuilder;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlTools;
import com.gitee.qdbp.jdbc.utils.DbTools;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.StringTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 表关联SQL片段生成帮助类
 *
 * @author 赵卉华
 * @version 190601
 */
public class TableJoinFragmentHelper extends TableQueryFragmentHelper {

    /** 构造函数 **/
    public TableJoinFragmentHelper(TableJoin tables) {
        super(DbTools.parseFieldColumns(tables));
    }

    /** {@inheritDoc} **/
    public SqlBuffer buildFromSql(boolean whole) {
        SqlBuffer buffer = new SqlBuffer();
        if (whole) {
            buffer.prepend("FROM", ' ');
        }
        return buffer;
    }

    /** {@inheritDoc} **/
    @Override
    public boolean containsField(String fieldName) {
        if (VerifyTools.isBlank(fieldName) || VerifyTools.isBlank(columns)) {
            return false;
        }
        return fieldColumnMap.containsKey(fieldName);
    }

    /** {@inheritDoc} **/
    @Override
    public String getColumnName(String fieldName, boolean throwOnNotFound) throws UnsupportedFieldExeption {
        int dotIndex = fieldName.lastIndexOf('.');
        String tableAlias = null;
        String realFieldName = fieldName;
        if (dotIndex == 0) {
            realFieldName = fieldName.substring(dotIndex + 1);
        } else if (dotIndex > 0) {
            tableAlias = fieldName.substring(0, dotIndex);
            realFieldName = fieldName.substring(dotIndex + 1);
        }
        String columnName = fieldColumnMap.get(realFieldName);
        if (VerifyTools.isBlank(columnName) && throwOnNotFound) {
            throw ufe("-", fieldName);
        }
        return StringTools.concat('.', tableAlias, columnName);
    }

    protected UnsupportedFieldExeption ufe(String subject, String field) {
        String message = subject + " unsupported fields";
        return new UnsupportedFieldExeption(clazz.getSimpleName(), message, Arrays.asList(field));
    }

    protected UnsupportedFieldExeption ufe(String subject, List<String> fields) {
        String message = subject + " unsupported fields";
        return new UnsupportedFieldExeption(clazz.getSimpleName(), message, fields);
    }
}
