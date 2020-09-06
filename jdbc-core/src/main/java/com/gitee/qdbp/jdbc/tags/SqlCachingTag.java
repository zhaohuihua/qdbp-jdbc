package com.gitee.qdbp.jdbc.tags;

import java.io.IOException;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.staticize.common.IContext;
import com.gitee.qdbp.staticize.common.IWriter;
import com.gitee.qdbp.staticize.exception.TagException;
import com.gitee.qdbp.staticize.tags.base.BufferTag;

/**
 * 带子标签内容缓冲区的标签处理类<br>
 * 某些标签需要根据子标签内容决定处理方式, 这就需要缓存所有子标签的输出
 * 
 * @author zhaohuihua
 * @version 20200906
 */
public abstract class SqlCachingTag extends BufferTag<SqlCachingWriter> {

    public SqlCachingTag() {
        super(new SqlCachingWriter());
    }

    @Override
    protected final void doEnded(IContext context, SqlCachingWriter buffer) throws TagException, IOException {
        doEnded(context, buffer.getBufferedContent());
    }

    protected void doEnded(IContext context, SqlBuffer buffer) throws TagException, IOException {
        context.write(buffer);
    }
}

class SqlCachingWriter implements IWriter {

    private SqlBuilder builder = new SqlBuilder();

    @Override
    public void write(Object value) throws IOException {
        if (value == null) {
            return;
        }
        if (value instanceof String) {
            this.builder.ad((String) value);
        } else if (value instanceof Character) {
            this.builder.ad((Character) value);
        } else if (value instanceof SqlBuffer) {
            this.builder.ad((SqlBuffer) value);
        } else if (value instanceof SqlBuilder) {
            this.builder.ad((SqlBuilder) value);
        } else {
            throw new IllegalArgumentException("UnsupportedArgumentType: " + value.getClass());
        }
    }

    public SqlBuffer getBufferedContent() {
        return this.builder.out();
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
