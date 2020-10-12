package com.gitee.qdbp.jdbc.tags;

import java.io.IOException;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlTools;
import com.gitee.qdbp.staticize.exception.TagException;
import com.gitee.qdbp.staticize.tags.base.BaseTag;
import com.gitee.qdbp.staticize.tags.base.NextStep;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 生成IN语句的标签
 *
 * @author zhaohuihua
 * @version 20201013
 * @since 3.2.0
 */
public class SqlInTag extends BaseTag {

    private String prefix;
    private String suffix;
    private String column;
    private Object value;
    private boolean not = false;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    @Override
    public NextStep doHandle() throws TagException, IOException {
        if (VerifyTools.isBlank(value)) {
            return NextStep.SKIP_BODY;
        }

        SqlDialect dialect = this.getStackValue("dialect", SqlDialect.class);
        SqlBuffer sql;
        if (not) {
            sql = SqlTools.buildNotInSql(column, ConvertTools.parseList(value), dialect);
        } else {
            sql = SqlTools.buildInSql(column, ConvertTools.parseList(value), dialect);
        }
        if (VerifyTools.isNotBlank(prefix)) {
            sql.shortcut().pd(prefix);
        }
        if (VerifyTools.isNotBlank(suffix)) {
            sql.shortcut().ad(suffix);
        }
        this.print(sql);
        return NextStep.SKIP_BODY;
    }
}
