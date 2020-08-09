package com.gitee.qdbp.jdbc.test.simple;

import com.alibaba.druid.pool.DruidDataSource;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.support.QdbcBootTools;
import com.gitee.qdbp.jdbc.test.enums.SettingState;
import com.gitee.qdbp.jdbc.test.model.SysSettingEntity;
import com.gitee.qdbp.tools.utils.JsonTools;

/**
 * 纯手工连接数据库测试类
 *
 * @author zhaohuihua
 * @version 20200809
 */
public class QdbcManualTest {

    public static void main(String[] args) {
        // 切换数据库配置即可连接不同的数据库
        testDbOperate("mysql:develop:dev2018lop@127.0.0.1:3306/qdbp-general");
        testDbOperate("oracle:qdbpgeneral:ora2018cle@127.0.0.1:1521/orcl");
        testDbOperate("db2:db2admin:db2020sql@127.0.0.1:50000/platform:qdbpgeneral");
    }

    private static void testDbOperate(String jdbcUrl) {
        // 1. 获取数据源对象
        try (DruidDataSource datasource = QdbcBootTools.buildDataSource(jdbcUrl);) {
            // 2. 获取数据库操作的构造器对象
            QdbcBoot boot = QdbcBootTools.buildByDataSource(datasource);
            // 3. 针对具体的实体, 生成增删改查DAO对象
            CrudDao<SysSettingEntity> dao = boot.buildCrudDao(SysSettingEntity.class);
            // 开始业务测试
            testSettingEntity(dao);
        }
    }

    private static void testSettingEntity(CrudDao<SysSettingEntity> dao) {
        { // 测试删除
            DbWhere where = new DbWhere();
            where.on("name", "starts", "HelloQdbc-");
            dao.physicalDelete(where);
        }
        String id;
        { // 测试新增
            SysSettingEntity entity = new SysSettingEntity();
            entity.setName("HelloQdbc-1");
            entity.setValue("测试新增 HelloQdbc-1");
            entity.setVersion(1);
            entity.setState(SettingState.ENABLED);
            id = dao.insert(entity);
            System.out.println("id = " + id);
        }
        { // 测试修改
            SysSettingEntity entity = new SysSettingEntity();
            entity.setId(id);
            entity.setValue("测试修改 HelloQdbc-1");
            entity.setVersion(2);
            dao.update(entity);
        }
        { // 测试查询
            SysSettingEntity entity = dao.findById(id);
            System.out.println(JsonTools.toLogString(entity));
        }
    }
}
