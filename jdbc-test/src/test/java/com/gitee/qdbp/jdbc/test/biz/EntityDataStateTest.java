package com.gitee.qdbp.jdbc.test.biz;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.able.jdbc.ordering.OrderPaging;
import com.gitee.qdbp.able.jdbc.ordering.Orderings;
import com.gitee.qdbp.able.jdbc.paging.PageList;
import com.gitee.qdbp.able.jdbc.paging.Paging;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.test.enums.DataState;
import com.gitee.qdbp.jdbc.test.model.SysLoggerEntity;
import com.gitee.qdbp.tools.utils.JsonTools;
import com.gitee.qdbp.tools.utils.StringTools;

@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class EntityDataStateTest extends AbstractTestNGSpringContextTests {

    private static Logger log = LoggerFactory.getLogger(EntityDataStateTest.class);

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private QdbcBoot qdbcBoot;
    private CrudDao<SysLoggerEntity> dao;
    private int total = 255;
    private int updateTotal = 25;
    private List<String> deletedIds;

    @PostConstruct
    public void init() {
        this.dao = qdbcBoot.buildCrudDao(SysLoggerEntity.class);

        DbWhere where = new DbWhere();
        where.on("name", "starts", "DsTest");
        dao.physicalDelete(where);
    }

    @Test(priority = 101)
    public void testBatchInsert() {
        // 构造实体数据
        List<SysLoggerEntity> entities = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            SysLoggerEntity entity = new SysLoggerEntity();
            String index = StringTools.pad(i, 4);
            entity.setName("DsTest-Insert-" + index);
            entity.setContent("DsTest-Content-" + index);
            entity.setSortIndex(i * 10);
            entities.add(entity);
        }
        // 执行批量新增
        dao.inserts(entities);
    }

    @Test(priority = 102, dependsOnMethods = "testBatchInsert")
    public void testAllRecordTotal() {
        // 检查记录总数
        DbWhere where = new DbWhere();
        where.on("name", "starts", "DsTest-Insert");
        int count = dao.count(where);
        Assert.assertEquals(count, total, "TotalRecord");
    }

    @Test(priority = 201, dependsOnMethods = "testBatchInsert")
    public void testLogicalDelete() {
        { // 查询一些数据的ID, 用来执行逻辑删除
            DbWhere where = new DbWhere();
            where.on("name", "starts", "DsTest-Insert");
            where.on("sortIndex", ">", 1);
            OrderPaging odpg = OrderPaging.of(new Paging(3, updateTotal, false), "sortIndex");
            PageList<String> temp = dao.listFieldValues("id", false, where, odpg, String.class);
            deletedIds = temp.toList();
        }
        { // 逻辑删除
            int deleteRows = dao.logicalDeleteByIds(deletedIds);
            Assert.assertEquals(deleteRows, deletedIds.size(), "LogicalDelete");
        }
    }

    @Test(priority = 202, dependsOnMethods = "testLogicalDelete")
    public void testDefaultDataStateCount() {
        // 根据默认数据状态过滤条件检查记录总数
        DbWhere where = new DbWhere();
        where.on("name", "starts", "DsTest");
        int count = dao.count(where);
        Assert.assertEquals(count, total - updateTotal, "DefaultDataState");
    }

    @Test(priority = 203, dependsOnMethods = "testLogicalDelete")
    public void testLogicalDeleteResult() {
        { // 测试默认数据状态过滤条件
            List<SysLoggerEntity> entities = dao.listByIds(deletedIds, Orderings.NONE);
            // 默认只查询有效记录, 因些期望为0
            Assert.assertEquals(entities.size(), 0, "DefaultDataState");
        }
        { // 测试数据状态=已删除的记录
            DbWhere where = new DbWhere();
            where.on("id", "in", deletedIds);
            where.on("dataState", "=", DataState.DELETED);
            List<SysLoggerEntity> entities = dao.list(where, Orderings.NONE);
            Assert.assertEquals(entities.size(), deletedIds.size(), "DataState=DELETED");
            log.info(JsonTools.toLogString(entities.get(0)));
        }
    }
}
