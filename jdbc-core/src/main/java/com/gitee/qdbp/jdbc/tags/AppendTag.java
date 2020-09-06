package com.gitee.qdbp.jdbc.tags;

import java.io.IOException;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.staticize.common.IContext;
import com.gitee.qdbp.staticize.exception.TagException;
import com.gitee.qdbp.staticize.tags.base.NextStep;

/**
 * 如果内容不为空, 则输出prefix+内容+suffix<br>
 * &lt;append prefix="AND"&gt;#{whereCondition}&lt;/append&gt;
 *
 * @author zhaohuihua
 * @version 20200906
 */
public class AppendTag extends SqlCachingTag {

    private Object prefix;
    private Object suffix;

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

    @Override
    public NextStep doHandle() throws TagException, IOException {
        return NextStep.EVAL_BODY;
    }

    @Override
    protected void doEnded(IContext context, SqlBuffer content) throws TagException, IOException {
        if (content.isBlank()) {
            return;
        }
        context.write(prefix);
        context.write(content);
        context.write(suffix);
    }
}
