package com.gitee.qdbp.jdbc.sql.parse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.utils.DbTools;
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
        // 设置预置数据
        if (VerifyTools.isNotBlank(preset)) {
            context.preset().putAll(preset);
        }
        // 设置全局预置变量
        presetGlobalVariable(context, dialect);
        // 生成SQL片段
        publish(context);
        return context.getSqlBuffer();
    }

    protected void presetGlobalVariable(SqlBufferContext context, SqlDialect dialect) {
        Map<String, Object> global = new HashMap<>();
        global.put("dialect", dialect);
        global.put("dbVersion", dialect.getDbVersion());
        global.put("dbType", dialect.getDbVersion().getDbType().name().toLowerCase());
        global.put("DbType", dialect.getDbVersion().getDbType().name());

        global.put("config", DbTools.getDbConfig());

        context.preset().put("db", global);
    }
}
