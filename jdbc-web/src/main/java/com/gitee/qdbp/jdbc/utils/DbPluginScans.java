package com.gitee.qdbp.jdbc.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import com.gitee.qdbp.jdbc.biz.CoreJdbcBootImpl;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.plugins.OrderBySqlBuilder;
import com.gitee.qdbp.jdbc.plugins.WhereSqlBuilder;
import com.gitee.qdbp.tools.utils.VerifyTools;

@Component
public class DbPluginScans implements ApplicationContextAware {

    /** 日志对象 **/
    private static final Logger log = LoggerFactory.getLogger(CoreJdbcBootImpl.class);

    private ApplicationContext context;

    @PostConstruct
    public void init() {
        // 查找所有的WhereSqlBuilder的子类
        Map<String, ?> whereBuilderInstances = context.getBeansOfType(WhereSqlBuilder.class);
        if (VerifyTools.isNotBlank(whereBuilderInstances)) {
            List<String> registered = new ArrayList<>();
            for (Object item : whereBuilderInstances.values()) {
                WhereSqlBuilder<?> builder = (WhereSqlBuilder<?>) item;
                DbPluginContainer.global.registerWhereSqlBuilder(builder);
                registered.add(builder.supported().getSimpleName());
            }
            log.debug("DbPluginRegister.global.registerWhereSqlBuilder({})", registered);
        }

        // 查找所有的OrderBySqlBuilder的子类
        Map<String, ?> orderByBuilderInstances = context.getBeansOfType(OrderBySqlBuilder.class);
        if (VerifyTools.isNotBlank(orderByBuilderInstances)) {
            List<String> registered = new ArrayList<>();
            for (Object item : orderByBuilderInstances.values()) {
                OrderBySqlBuilder<?> builder = (OrderBySqlBuilder<?>) item;
                DbPluginContainer.global.registerOrderBySqlBuilder(builder);
            }
            log.debug("DbPluginRegister.global.registerOrderBySqlBuilder({})", registered);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }
    
}
