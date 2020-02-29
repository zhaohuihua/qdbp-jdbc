package com.gitee.qdbp.jdbc.plugins.impl;

import com.gitee.qdbp.jdbc.plugins.EntityFillBizResolver;
import com.gitee.qdbp.tools.utils.RandomTools;

/**
 * 实体数据填充, 处理与业务强相关的实体数据<br>
 * 获取当前登录用户的账号/生成实体类的主键
 *
 * @author zhaohuihua
 * @version 20200128
 */
public class SimpleEntityFillBizResolver implements EntityFillBizResolver {

    @Override
    public String getLoginAccount() {
        return null;
    }

    @Override
    public String generatePrimaryKeyCode(String tableName) {
        return RandomTools.generateUuid();
    }

}
