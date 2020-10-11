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

    private SqlBuffer caching = new SqlBuffer();

    @Override
    public void write(Object value) throws IOException {
        if (value == null) {
            return;
        }
        if (value instanceof String) {
            this.caching.append((String) value);
        } else if (value instanceof Character) {
            this.caching.append((Character) value);
        } else if (value instanceof SqlBuffer) {
            this.caching.append((SqlBuffer) value);
        } else if (value instanceof SqlBuilder) {
            this.caching.append(((SqlBuilder) value).out());
        } else {
            throw new IllegalArgumentException("UnsupportedArgumentType: " + value.getClass());
        }
    }

    public SqlBuffer getContent() {
        return this.caching;
    }

    public void clear() {
        this.caching.clear();
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
