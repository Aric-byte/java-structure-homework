package com.Spring.dao.annotation;

import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@ComponentScan(basePackages = "com.Spring.dao.annotation")
@PropertySource("classpath:common/db.properties")
@EnableTransactionManagement //事务开启的开关
public class DaoAnnotationConfig {

    @Bean //定义一个Bean，创建数据源
    public DataSource dataSource(Environment env) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(env.getProperty("jdbc.driver"));
        ds.setJdbcUrl(env.getProperty("jdbc.url"));
        ds.setUsername(env.getProperty("jdbc.username"));
        ds.setPassword(env.getProperty("jdbc.password"));
        ds.setMaximumPoolSize(10);
        ds.setMinimumIdle(5);
        ds.setConnectionTimeout(30000);
        ds.setIdleTimeout(600000);
        ds.setMaxLifetime(1800000);
        return ds;
    }

    @Bean
    public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource) throws IOException {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setConfigLocation(
                new ClassPathResource("springdao/annotation/mybatis-config.xml"));
        factory.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:springdao/annotation/**/*Mapper.xml"));
        factory.setTypeAliasesPackage("com.Spring.dao.annotation");
        return factory;
    }

    @Bean
    public static MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer scanner = new MapperScannerConfigurer();
        scanner.setBasePackage("com.Spring.dao.annotation");
        scanner.setSqlSessionFactoryBeanName("sqlSessionFactory");
        return scanner;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}