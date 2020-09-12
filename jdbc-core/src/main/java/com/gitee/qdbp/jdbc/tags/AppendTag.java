package com.gitee.qdbp.jdbc.tags;

import java.io.IOException;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.staticize.common.IWriter;
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

    private String prefix;
    private String suffix;

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

    @Override
    public NextStep doHandle() throws TagException, IOException {
        return NextStep.EVAL_BODY;
    }

    @Override
    protected void doEnded(SqlBuffer buffer, IWriter writer) throws TagException, IOException {
        if (buffer.isBlank()) {
            return;
        }
        buffer.insertPrefix(prefix, null);
        buffer.insertSuffix(suffix, null);
        writer.write(buffer);
    }
}
