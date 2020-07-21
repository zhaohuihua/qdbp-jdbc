package com.gitee.qdbp.jdbc.test.biz;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;
import com.gitee.qdbp.able.jdbc.condition.DbWhere;
import com.gitee.qdbp.jdbc.api.CrudDao;
import com.gitee.qdbp.jdbc.api.QdbcBoot;
import com.gitee.qdbp.jdbc.test.model.SysUserEntity;

/**
 * IN条件测试
 *
 * @author zhaohuihua
 * @version 20200721
 */
@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class InConditionTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private QdbcBoot qdbcBoot;

    @Test(priority = 2)
    public void testInQuery() {
        List<String> values = new ArrayList<>();
        for (int i = 0; i < 1500; i++) {
            values.add(calcCode(i));
        }
        DbWhere where = new DbWhere();
        where.on("deptCode", "in", values);
        CrudDao<SysUserEntity> dao = qdbcBoot.buildCrudDao(SysUserEntity.class);
        System.out.println(dao.count(where));
    }

    private static String calcCode(int number) {
        int offset = '0' - 'A';
        char[] chars = String.valueOf(number).toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] - offset);
        }
        return new String(chars);
    }
}
