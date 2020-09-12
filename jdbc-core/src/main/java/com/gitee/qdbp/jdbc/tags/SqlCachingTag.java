package com.gitee.qdbp.jdbc.tags;

import java.io.IOException;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.parse.SqlCachingWriter;
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
    protected final void doEnded(SqlCachingWriter buffer, IWriter origin) throws TagException, IOException {
        doEnded(buffer.getBufferedContent(), origin);
    }

    /**
     * 标签结束的处理, 一般是将缓冲区内容写入原IWriter
     * 
     * @param buffer 缓冲区内容
     * @param writer 原Writer
     * @throws TagException 标签处理异常
     * @throws IOException 输出处理异常
     */
    protected abstract void doEnded(SqlBuffer buffer, IWriter writer) throws TagException, IOException;
}

