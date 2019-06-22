package com.gitee.qdbp.jdbc.plugins.impl;

import com.gitee.qdbp.jdbc.plugins.NameConverter;
import com.gitee.qdbp.tools.utils.NamingTools;

public abstract class BaseNameConverter implements NameConverter {

    private boolean columnNameUseUpperCase = true;

    public boolean isColumnNameUseUpperCase() {
        return columnNameUseUpperCase;
    }

    public void setColumnNameUseUpperCase(boolean columnNameUseUpperCase) {
        this.columnNameUseUpperCase = columnNameUseUpperCase;
    }

    protected String toUnderlineString(String string) {
        String result = NamingTools.toUnderlineString(string);
        return columnNameUseUpperCase ? result.toUpperCase() : result.toLowerCase();
    }

}
