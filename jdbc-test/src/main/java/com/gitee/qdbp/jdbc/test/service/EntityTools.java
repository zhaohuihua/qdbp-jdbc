package com.gitee.qdbp.jdbc.test.service;

import java.util.Arrays;
import java.util.List;
import com.gitee.qdbp.jdbc.plugins.SqlDialect;
import com.gitee.qdbp.jdbc.sql.SqlBuffer;
import com.gitee.qdbp.jdbc.sql.SqlTools;

/**
 * 实体类工具
 *
 * @author zhaohuihua
 * @version 20201007
 */
public class EntityTools {
    
    private static List<String> getCurrDeptIdsFromSession() {
        // 模拟从SESSION中获取当前登录用户有权限的部门ID
        return Arrays.asList("D0000001", "D0000005", "D0000012");
    }

    /** 生成部门数据权限查询条件 **/
    public static SqlBuffer buildDeptDataPermission(String orgColumn, SqlDialect dialect) {
        List<String> deptIds = getCurrDeptIdsFromSession();
        return SqlTools.buildInSql(orgColumn, deptIds, dialect);
    }
}
