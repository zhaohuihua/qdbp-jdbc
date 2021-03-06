package com.gitee.qdbp.jdbc.test.utils;

import java.awt.Image;
import java.io.File;
import com.gitee.qdbp.able.matches.EqualsStringMatcher;
import com.gitee.qdbp.jdbc.plugins.DbPluginContainer;
import com.gitee.qdbp.jdbc.plugins.EntityFillBizResolver;
import com.gitee.qdbp.jdbc.plugins.impl.DataSourceDbVersionFinder;
import com.gitee.qdbp.jdbc.plugins.impl.PersistenceAnnotationTableScans;
import com.gitee.qdbp.jdbc.plugins.impl.RandomNumberEntityDataStateFillStrategy;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleCommonFieldResolver;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleDbOperatorContainer;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleEntityFieldFillStrategy;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleNameConverter;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleSqlFormatter;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleTableInfoScans;
import com.gitee.qdbp.jdbc.plugins.impl.SimpleTableNameScans;
import com.gitee.qdbp.jdbc.plugins.impl.SpringMapToBeanConverter;
import com.gitee.qdbp.jdbc.plugins.impl.SpringVarToDbValueConverter;
import com.gitee.qdbp.jdbc.plugins.impl.StaticFieldTableNameScans;
import com.gitee.qdbp.jdbc.support.QdbcBootBaseFactory;
import com.gitee.qdbp.jdbc.test.enums.DataState;

/**
 * QdbcBoot工厂类, 手动设置DbPluginContainer
 *
 * @author zhaohuihua
 * @version 20200129
 */
public class QdbcBootManualFactory extends QdbcBootBaseFactory {

    /** 与业务强相关的数据提供者 **/
    private EntityFillBizResolver entityFillBizResolver;

    @Override
    public void afterPropertiesSet() {
        DbPluginContainer plugins = new DbPluginContainer();
        registerTableInfoScans(plugins, true);
        registerToDbValueConverter(plugins);
        registerMapToBeanConverter(plugins);
        registerEntityFieldFillStrategy(plugins);
        registerEntityDataStateFillStrategy(plugins);
        // 设置数据库版本信息查询方式
        // DataSourceDbVersionFinder: 根据数据源查找数据库类型和版本信息
        plugins.setDbVersionFinder(new DataSourceDbVersionFinder());
        // 设置运算符容器
        plugins.setOperatorContainer(new SimpleDbOperatorContainer());
        // 设置SQL格式化处理类
        plugins.setSqlFormatter(new SimpleSqlFormatter());
        this.setPluginContainer(plugins);
        super.afterPropertiesSet();
    }

    private void registerEntityFieldFillStrategy(DbPluginContainer plugins) {
        SimpleEntityFieldFillStrategy trategy = new SimpleEntityFieldFillStrategy();
        // 创建用户字段名
        trategy.setCreateUserField("creatorId");
        // 创建时间字段名
        trategy.setCreateTimeField("createTime");
        // 更新用户字段名
        trategy.setUpdateUserField("updatorId");
        // 修改时间字段名
        trategy.setCreateTimeField("updateTime");
        // 与业务强相关的数据提供者
        if (entityFillBizResolver != null) {
            trategy.setEntityFillBizResolver(entityFillBizResolver);
        }
        plugins.setEntityFieldFillStrategy(trategy);
    }

    private void registerEntityDataStateFillStrategy(DbPluginContainer plugins) {
        RandomNumberEntityDataStateFillStrategy<DataState> strategy = new RandomNumberEntityDataStateFillStrategy<>();
        // 逻辑删除字段名
        strategy.setLogicalDeleteField("dataState");
        // 数据有效标记
        strategy.setDataEffectiveFlag(DataState.NORMAL);
        // 数据无效标记
        strategy.setDataIneffectiveFlag(DataState.DELETED);
        plugins.setEntityDataStateFillStrategy(strategy);
    }

    private void registerToDbValueConverter(DbPluginContainer plugins) {
        // 数据转换处理类
        SpringVarToDbValueConverter converter = new SpringVarToDbValueConverter();
        // 设置Spring的类型转换处理类
        converter.setConversionService(this.getConversionService());
        // 设置枚举是否默认使用ordinal: true=ordinal, false=name 
        converter.setEnumConvertUseOrdinal(true);
        // 设置枚举例外列表: 以下列表不使用ordinal的(即使用name的)
        // converter.addEnumConvertEspecialList(DataState.class);
        // 设置未知类型对象转换是否启用: true=执行convertToString()转换, false=不转换直接返回对象(由JDBC处理) 
        converter.setUntypedObjectConvertEnabled(true);
        // 设置类型对象转换的例外列表: 以下列表不执行convertToString转换
        converter.addUntypedObjectConvertEspecialList(File.class);
        converter.addUntypedObjectConvertEspecialList(Image.class);
        // 设置对象(基本对象除外)转字符串是否默认使用JSON格式
        converter.setObjectToStringUseJson(true);
        // 设置对象转字符串例外列表: 以下列表不使用JSON格式(即使用toString方法)
        // converter.addObjectToStringEspecialList(Xxx.class);
        // converter.addObjectToStringEspecialList(Yyy.class);
        plugins.setToDbValueConverter(converter);
    }

    private void registerMapToBeanConverter(DbPluginContainer plugins) {
        SpringMapToBeanConverter converter = new SpringMapToBeanConverter();
        converter.setConversionService(this.getConversionService());
        plugins.setMapToBeanConverter(converter);
    }

    private void registerTableInfoScans(DbPluginContainer plugins, boolean scanAnnotation) {
        // TableInfoScans, 扫描数据表和列信息, 已提供两个实现类:
        // PersistenceAnnotationTableScans: 扫描@Table/@Column/@Id注解
        // SimpleTableInfoScans: 提取全部字段, 而不是扫描注解
        if (scanAnnotation) {
            registerPersistenceAnnotationTableScans(plugins);
        } else {
            registerSimpleTableInfoScans(plugins);
        }
    }

    private void registerPersistenceAnnotationTableScans(DbPluginContainer plugins) {
        // PersistenceAnnotationTableScans: 扫描@Table/@Column/@Id注解
        PersistenceAnnotationTableScans scans = new PersistenceAnnotationTableScans();
        // 主键查找方式: 查找字段名为id的字段
        // 仅在useMissAnnotationField=true时使用
        // scans.setPrimaryKeyMatcher(new EqualsStringMatcher("id"));
        // 表名/字段名的转换器: java的驼峰命名与数据库的下划线命名之间的转换
        scans.setNameConverter(new SimpleNameConverter());
        // SimpleTableNameScans: 扫描java类的@Table注解, 如果没有注解则将java类名转换为下划线形式的表名
        scans.setTableNameScans(new SimpleTableNameScans());
        // 判断是否公共字段的处理器(为了将公共字段排在最后)
        SimpleCommonFieldResolver commonFieldResolver = new SimpleCommonFieldResolver();
        // 可以配置父类的包名, 如 实体类都继承com.xxx.core.CommonEntity, 那么可以设置com.xxx.core为公共包名
        // commonFieldResolver.addCommonPackageMatchers(CommonEntity.class.getPackage().getName());
        // 也可以配置公共字段名, 如 creatorId, createTime, updateTime, dataState等
        commonFieldResolver.addCommonFieldNameMatchers("creatorId", "createTime", "updateTime", "dataState");
        scans.setCommonFieldResolver(commonFieldResolver);
        plugins.setTableInfoScans(scans);
    }

    private void registerSimpleTableInfoScans(DbPluginContainer plugins) {
        // SimpleTableInfoScans: 提取全部字段, 而不是扫描注解
        SimpleTableInfoScans scans = new SimpleTableInfoScans();
        // 主键查找方式: 查找字段名为id的字段
        scans.setPrimaryKeyMatcher(new EqualsStringMatcher("id"));
        // 表名/字段名的转换器: java的驼峰命名与数据库的下划线命名之间的转换
        scans.setNameConverter(new SimpleNameConverter());
        // 表名扫描类: 如果从java类信息中找到表名, 已提供两个实现类:
        // SimpleTableNameScans: 扫描java类的@Table注解, 如果没有注解则将java类名转换为下划线形式的表名
        // StaticFieldTableNameScans: 从静态字段中获取表名
        scans.setTableNameScans(new StaticFieldTableNameScans("TABLE"));
        // 判断是否公共字段的处理器(为了将公共字段排在最后)
        SimpleCommonFieldResolver commonFieldResolver = new SimpleCommonFieldResolver();
        // 可以配置父类的包名, 如 实体类都继承com.xxx.core.CommonEntity, 那么可以设置com.xxx.core为公共包名
        // commonFieldResolver.addCommonPackageMatchers(CommonEntity.class.getPackage().getName());
        // 也可以配置公共字段名, 如 creatorId, createTime, updateTime, dataState等
        commonFieldResolver.addCommonFieldNameMatchers("creatorId", "createTime", "updateTime", "dataState");
        scans.setCommonFieldResolver(commonFieldResolver);
        plugins.setTableInfoScans(scans);
    }

    /** 与业务强相关的数据提供者 **/
    public EntityFillBizResolver getEntityFillBizResolver() {
        return entityFillBizResolver;
    }

    /** 与业务强相关的数据提供者 **/
    public void setEntityFillBizResolver(EntityFillBizResolver entityFillBizResolver) {
        this.entityFillBizResolver = entityFillBizResolver;
    }

}
