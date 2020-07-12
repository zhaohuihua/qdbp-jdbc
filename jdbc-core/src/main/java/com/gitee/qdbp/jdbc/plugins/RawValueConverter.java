package com.gitee.qdbp.jdbc.plugins;

/**
 * 数据库原生值的转换(sysdate, CURRENT_TIMESTAMP等)<br>
 * 是为了弥补各个数据库之间原生值的差异而作出的努力<br>
 * 为实现常规增删改查操作一套代码适应各个数据库<br>
 * 在做字段默认值时遇到了这个问题, 默认值需要通过注解配置在java代码中, 但各数据库无法达到一致<br>
 * 因此, 配置时填为oracle或mysql的常用值, 再通过RawValueConverter转换为各数据库的方言<br>
 * 预期目标: &#064;ColumnDefault("sysdate");<br>
 * 在mysql/db2/PostgreSQL转换为CURRENT_TIMESTAMP, SqlServer转换为GETDATE(), ...<br>
 *
 * @author zhaohuihua
 * @version 20190709
 */
public interface RawValueConverter {

    /**
     * 将配置的原生值转换为指定数据库类型的可识别的原生值<br>
     * 注意: 转换后的值在指定数据库下应能放在INSERT的VALUES中执行
     * 
     * @param value 原生值, 如sysdate
     * @param dialect 数据库方言
     * @return 转换后的值, 如mysql的CURRENT_TIMESTAMP, SqlServer的GETDATE()
     */
    String convert(String value, SqlDialect dialect);
}
