package com.gitee.qdbp.jdbc.tags;

import java.io.IOException;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.staticize.exception.TagException;
import com.gitee.qdbp.staticize.tags.base.BaseTag;
import com.gitee.qdbp.staticize.tags.base.NextStep;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 生成Like语句的标签
 *
 * @author zhaohuihua
 * @version 20201013
 * @since 3.2.0
 */
public class SqlLikeTag extends BaseTag {

    private String prefix;
    private String suffix;
    private String column;
    private Object value;
    private boolean not = false;
    private String type; // starts|ends|both

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public NextStep doHandle() throws TagException, IOException {
        if (VerifyTools.isBlank(value)) {
            return NextStep.SKIP_BODY;
        }

        SqlDialect dialect = this.getStackValue("db.dialect", SqlDialect.class);
        if (dialect == null) {
            throw new TagException("Context variable of '${db.dialect}' is null");
        }
        SqlBuilder sql = new SqlBuilder();
        if (VerifyTools.isNotBlank(prefix)) {
            sql.ad(prefix);
        }
        if (not) {
            sql.ad("NOT");
        }
        if (type == null) {
            sql.ad(dialect.buildLikeSql(value));
        } else if ("starts".equalsIgnoreCase(type) || "StartsWith".equalsIgnoreCase(type)) {
            sql.ad(dialect.buildStartsWithSql(value));
        } else if ("ends".equalsIgnoreCase(type) || "EndsWith".equalsIgnoreCase(type)) {
            sql.ad(dialect.buildEndsWithSql(value));
        } else {
            sql.ad(dialect.buildLikeSql(value));
        }
        if (VerifyTools.isNotBlank(suffix)) {
            sql.ad(suffix);
        }
        this.print(sql);
        return NextStep.SKIP_BODY;
    }
}
