package com.gitee.qdbp.jdbc.tags;

import java.io.IOException;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.staticize.common.IContext;
import com.gitee.qdbp.staticize.exception.TagException;
import com.gitee.qdbp.staticize.tags.base.NextStep;

/**
 * 去掉第1个prefixOverrides, 去掉最后一个suffixOverrides<br>
 * 在前面增加prefix, 在后面增加suffix<br>
 *
 * @author zhaohuihua
 * @version 20200906
 */
public class TrimTag extends SqlCachingTag {

    private Object prefix;
    private Object suffix;
    private String prefixOverrides;
    private String suffixOverrides;

    public Object getPrefix() {
        return prefix;
    }

    public void setPrefix(Object prefix) {
        this.prefix = prefix;
    }

    public Object getSuffix() {
        return suffix;
    }

    public void setSuffix(Object suffix) {
        this.suffix = suffix;
    }

    public String getPrefixOverrides() {
        return prefixOverrides;
    }

    public void setPrefixOverrides(String prefixOverrides) {
        this.prefixOverrides = prefixOverrides;
    }

    public String getSuffixOverrides() {
        return suffixOverrides;
    }

    public void setSuffixOverrides(String suffixOverrides) {
        this.suffixOverrides = suffixOverrides;
    }

    @Override
    public NextStep doHandle() throws TagException, IOException {
        return NextStep.EVAL_BODY;
    }

    @Override
    protected void doEnded(IContext context, SqlBuffer content) throws TagException, IOException {
        if (content.isBlank()) {
            return;
        }
        content.addPrefix(prefix, prefixOverrides);
        context.write(content);
        content.addSuffix(suffix, suffixOverrides);
    }
}
