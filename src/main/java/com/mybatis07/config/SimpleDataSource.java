package com.mybatis07.config;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * MyBatis-Plus 纯 Java 配置用的简易 DataSource 实现
 *
 * 说明：
 *   1. 不依赖 Spring，仅通过 DriverManager 获取连接
 *   2. 适合演示/测试场景；生产环境应使用连接池（Druid / HikariCP）
 *   3. 实现 javax.sql.DataSource 接口，供 MybatisSqlSessionFactoryBean 使用
 */
public class SimpleDataSource implements DataSource {

    private final String driver;
    private final String url;
    private final String username;
    private final String password;

    public SimpleDataSource(String driver, String url, String username, String password) {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException(e);
        }
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public Connection getConnection(String u, String p) throws SQLException {
        return getConnection();
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
    }

    @Override
    public void setLoginTimeout(int seconds) {
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() {
        return null;
    }
}