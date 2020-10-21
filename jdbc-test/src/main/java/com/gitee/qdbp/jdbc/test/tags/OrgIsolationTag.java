package com.gitee.qdbp.jdbc.test.tags;

import java.io.IOException;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.test.service.EntityTools;
import com.gitee.qdbp.staticize.exception.TagException;
import com.gitee.qdbp.staticize.tags.base.BaseTag;
import com.gitee.qdbp.staticize.tags.base.NextStep;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 生成机构数据权限隔离条件的标签
 *
 * @author zhaohuihua
 * @version 20201013
 * @since 3.2.0
 */
public class OrgIsolationTag extends BaseTag {

    private String prefix;
    private String suffix;
    private String column;

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

    @Override
    public NextStep doHandle() throws TagException, IOException {
        SqlDialect dialect = this.getStackValue("db.dialect", SqlDialect.class);
        if (dialect == null) {
            throw new TagException("Context variable of '${db.dialect}' is null");
        }
        SqlBuffer sql = EntityTools.buildOrgDataPermission(column, dialect);
        if (sql == null || sql.isBlank()) {
            return NextStep.SKIP_BODY;
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
