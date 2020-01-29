package com.gitee.qdbp.jdbc.plugins;

/**
 * 实体数据填充<br>
 * 与业务强相关的方法单独抽取一个接口, 方便业务侧提供实现类
 *
 * @author zhaohuihua
 * @version 200128
 */
public interface EntityFillBizResolver {

    /** 获取当前登录账号, 一般是UserId **/
    String getLoginAccount();

    /** 生成主键编号 **/
    String generatePrimaryKeyCode(String tableName);

}
