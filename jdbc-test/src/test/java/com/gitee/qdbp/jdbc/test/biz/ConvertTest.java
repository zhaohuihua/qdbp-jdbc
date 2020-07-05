package com.gitee.qdbp.jdbc.test.biz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import com.gitee.qdbp.jdbc.test.enums.AccountType;
import com.gitee.qdbp.jdbc.test.enums.UserType;

@ContextConfiguration(locations = { "classpath:settings/spring/spring.xml" })
public class ConvertTest extends AbstractTestNGSpringContextTests {

    private Logger log = LoggerFactory.getLogger(SimpleCrudDaoTest.class);

    @Autowired
    private ConversionService conversionService;

    @Test(priority = 1)
    public void testCanConvertAccountType() {
        boolean canConvertStringToAccountType = conversionService.canConvert(String.class, AccountType.class);
        boolean canConvertIntegerToAccountType = conversionService.canConvert(Integer.class, AccountType.class);
        boolean canConvertIntToAccountType = conversionService.canConvert(int.class, AccountType.class);
        log.debug("canConvertStringToAccountType: {}", canConvertStringToAccountType);
        log.debug("canConvertIntegerToAccountType: {}", canConvertIntegerToAccountType);
        log.debug("canConvertIntToAccountType: {}", canConvertIntToAccountType);
        Assert.assertTrue(canConvertStringToAccountType, "CanConvertStringToAccountType");
        Assert.assertTrue(canConvertIntegerToAccountType, "CanConvertIntegerToAccountType");
        Assert.assertTrue(canConvertIntToAccountType, "CanConvertIntToAccountType");
    }

    @Test(priority = 2)
    public void testConvertAccountType() {
        AccountType fromStr = conversionService.convert("ADMIN", AccountType.class);
        AccountType fromInt = conversionService.convert(0, AccountType.class);
        log.debug("AccountTypeFromString: {}, class: {}", fromStr, fromStr == null ? null : fromStr.getClass());
        log.debug("AccountTypeFromInt: {}, class: {}", fromInt, fromInt == null ? null : fromInt.getClass());
        Assert.assertEquals(fromStr, UserType.ADMIN);
        Assert.assertEquals(fromInt, UserType.SYSTEM);
    }
}
