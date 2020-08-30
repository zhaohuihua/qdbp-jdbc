package com.gitee.qdbp.jdbc.sql.parse;

import com.gitee.qdbp.jdbc.plugins.SqlDialect;

public class SqlFragmentRenderer {

    private SqlDialect dialect;
    private SqlFragmentContainer container;

    public SqlFragmentRenderer(SqlFragmentContainer container, SqlDialect dialect) {
        this.container = container;
        this.dialect = dialect;
    }

    
}
