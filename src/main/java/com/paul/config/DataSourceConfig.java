package com.paul.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.decaywood.entity.StockInfo;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.batch.MyBatisCursorItemReader;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import tk.mybatis.spring.mapper.MapperScannerConfigurer;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by liusonglin
 * Date:2017/9/20
 * Description:
 */
@Configuration
@Slf4j
public class DataSourceConfig {

    @Bean("dataSource")
    public DataSource dataSource(){
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://localhost:3306/stock?useUnicode=true&characterEncoding=UTF-8");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("111111");
        druidDataSource.setInitialSize(10);
        druidDataSource.setAsyncCloseConnectionEnable(true);
        druidDataSource.setMaxActive(100);
        druidDataSource.setTestOnBorrow(true);
        druidDataSource.setTimeBetweenConnectErrorMillis(1000);
        druidDataSource.setTransactionThresholdMillis(3000);
        return druidDataSource;
    }

    @Bean
    public PlatformTransactionManager annotationDrivenTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean("sqlSessionFactory")
    @DependsOn("dataSource")
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource){
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setTypeAliasesPackage("org.decaywood.entity");

        /*分页插件*/
        PageHelper pageHelper = new PageHelper();
        Properties properties = new Properties();
        properties.setProperty("reasonable", "true");
        properties.setProperty("supportMethodsArguments", "true");
        properties.setProperty("returnPageInfo", "check");
        properties.setProperty("params", "count=countSql");
        pageHelper.setProperties(properties);


        sqlSessionFactoryBean.setPlugins(new Interceptor[]{pageHelper});

        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

        try {
            sqlSessionFactoryBean.setMapperLocations(resourcePatternResolver.getResources("classpath:mapper/*.xml"));
            return sqlSessionFactoryBean.getObject();
        }  catch (Exception e) {
            e.printStackTrace();
            log.error("create sql session error",e);
        }

        return null;
    }



    @Bean
    @DependsOn("sqlSessionFactory")
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory){
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer(){
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();

        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        mapperScannerConfigurer.setBasePackage("com.paul.mapper");
        Properties properties = new Properties();
        properties.setProperty("mappers", "com.paul.utils.MyMapper");
        properties.setProperty("notEmpty", "true");
        properties.setProperty("IDENTITY", "MYSQL");
        mapperScannerConfigurer.setProperties(properties);
        return mapperScannerConfigurer;
    }

    @Bean
    public MyBatisCursorItemReader<StockInfo> myBatisCursorItemReader(SqlSessionFactory sqlSessionFactory) throws Exception {
        MyBatisCursorItemReader<StockInfo> cursorItemReader = new MyBatisCursorItemReader();
        cursorItemReader.setQueryId("com.paul.mapper.StockInfoMapper.select");
        cursorItemReader.setSqlSessionFactory(sqlSessionFactory);
        cursorItemReader.afterPropertiesSet();
        return cursorItemReader;
    }

    @Bean
    public MyBatisPagingItemReader<StockInfo> myBatisPagingItemReader(SqlSessionFactory sqlSessionFactory) throws Exception {
        MyBatisPagingItemReader<StockInfo> pagingItemReader = new MyBatisPagingItemReader();
        pagingItemReader.setQueryId("com.paul.mapper.StockInfoMapper.select");
        pagingItemReader.setSqlSessionFactory(sqlSessionFactory);
        pagingItemReader.afterPropertiesSet();
        return pagingItemReader;
    }
}
