package com.gitee.qdbp.jdbc.sql.parse;

import java.io.IOException;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.staticize.common.IWriter;

/**
 * 缓存SQL的Writer
 *
 * @author zhaohuihua
 * @version 20200912
 */
public class SqlCachingWriter implements IWriter {

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