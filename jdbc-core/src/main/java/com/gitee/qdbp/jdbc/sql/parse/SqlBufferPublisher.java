package com.gitee.qdbp.jdbc.sql.parse;

import java.io.IOException;
import java.util.Map;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.staticize.common.IMetaData;
import com.gitee.qdbp.staticize.exception.TagException;
import com.gitee.qdbp.staticize.publish.BasePublisher;
import com.gitee.qdbp.tools.utils.VerifyTools;

/**
 * 根据标签数据发布一个SQL片段, 输出到SqlBuffer<br>
 * 不支持多线程<br>
 *
 * @author zhaohuihua
 * @version 20200912
 * @since 3.2.0
 */
public class SqlBufferPublisher extends BasePublisher {

    /**
     * 构造函数
     *
     * @param root 根节点
     */
    public SqlBufferPublisher(IMetaData root) {
        super(root);
    }

    /**
     * 根据标签数据发布一个SQL片段
     *
     * @param preset 预置数据
     * @param dialect 数据库方言处理类
     * @throws TagException 标签错误
     * @throws IOException 写文件失败
     */
    public SqlBuffer publish(Map<String, Object> preset, SqlDialect dialect) throws TagException, IOException {
        SqlBufferContext context = new SqlBufferContext(dialect);
        context.preset().put("dialect", dialect);
        context.preset().put("dbVersion", dialect.getDbVersion());
        context.preset().put("dbType", dialect.getDbVersion().getDbType().name().toLowerCase());
        context.preset().put("DbType", dialect.getDbVersion().getDbType().name());
        if (VerifyTools.isNotBlank(preset)) {
            context.preset().putAll(preset);
        }

        publish(context);
        return context.getSqlBuffer();
    }
}
