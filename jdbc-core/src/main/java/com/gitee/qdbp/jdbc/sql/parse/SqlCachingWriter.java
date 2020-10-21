package com.gitee.qdbp.jdbc.sql.parse;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlBuilder;
import com.gitee.qdbp.staticize.common.IWriter;
import com.gitee.qdbp.tools.utils.ConvertTools;
import com.gitee.qdbp.tools.utils.DateTools;

/**
 * 缓存SQL的Writer
 *
 * @author zhaohuihua
 * @version 20200912
 * @since 3.2.0
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
        } else if (value instanceof SqlBuffer) {
            this.caching.append((SqlBuffer) value);
        } else if (value instanceof SqlBuilder) {
            this.caching.append(((SqlBuilder) value).out());
        } else if (value instanceof Character) {
            this.caching.append((Character) value);
        } else if (value instanceof CharSequence) {
            this.caching.append(value.toString());
        } else if (value instanceof Date) {
            this.caching.append(DateTools.toNormativeString((Date) value));
        } else if (value instanceof Collection || value.getClass().isArray()) {
            List<Object> values = ConvertTools.parseList(value);
            this.caching.append(ConvertTools.joinToString(values, ',', false));
        } else {
            this.caching.append(value.toString());
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
