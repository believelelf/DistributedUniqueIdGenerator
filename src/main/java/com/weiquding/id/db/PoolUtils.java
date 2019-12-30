package com.weiquding.id.db;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbcp2.*;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * 数据库连接池相关操作
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/11/27
 */
public class PoolUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolUtils.class);

    private static final String JDBC_PROPERTIES_NAME = "/jdbc.properties";

    private static final String PREFIX = "jdbc.";

    private static final JdbcConfig JDBC_CONFIG = new JdbcConfig();

    private static volatile DataSource DATA_SOURCE;

    static {
        try {
            Properties properties = new Properties();
            properties.load(PoolUtils.class.getResourceAsStream(JDBC_PROPERTIES_NAME));
            properties.forEach((key, value) -> {
                try {
                    BeanUtils.setProperty(JDBC_CONFIG, ((String) key).replace(PREFIX, ""), value);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    LOGGER.error("An error occurred while copying a property", e);
                }
            });
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load configuration file");
        }
    }

    /**
     * 获取连接
     *
     * @return 数据库连接
     */
    public static Connection getConnection() {
        try {
            return createDataSource().getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("An error occurred while obtaining the connection", e);
        }
    }

    /**
     * 关闭资源
     *
     * @param connection Connection
     * @param statement  Statement
     * @param resultSet  ResultSet
     */
    public static void close(Connection connection, Statement statement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.warn("An error occurred while closing ResultSet", e);
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOGGER.warn("An error occurred while closing Statement", e);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.warn("An error occurred while closing Connection", e);
            }
        }
    }

    /**
     * 创建连接池
     *
     * @return DataSource
     */
    public static DataSource createDataSource() {
        // Return the pool if we have already created it
        // This is double-checked locking. This is safe since dataSource is
        // volatile and the code is targeted at Java 5 onwards.
        if (DATA_SOURCE != null) {
            return DATA_SOURCE;
        }
        synchronized (PoolUtils.class) {
            if (DATA_SOURCE != null) {
                return DATA_SOURCE;
            }
            DATA_SOURCE = createDataSource(JDBC_CONFIG);
            return DATA_SOURCE;
        }
    }

    /**
     * 创建DataSource
     *
     * @param jdbcConfig 属性配置文件
     * @return DataSource
     */
    private static DataSource createDataSource(JdbcConfig jdbcConfig) {

        try {
            Class.forName(jdbcConfig.getDriverClassName());
        } catch (ClassNotFoundException e) {
            LOGGER.error("An error occurred while loading the driver", e);
        }
        //
        // First, we'll create a ConnectionFactory that the
        // pool will use to create Connections.
        // We'll use the DriverManagerConnectionFactory,
        // using the connect string passed in the command line
        // arguments.
        //
        ConnectionFactory connectionFactory =
                new DriverManagerConnectionFactory(jdbcConfig.getUrl(), jdbcConfig.getUsername(), jdbcConfig.getPassword());
        //
        // Next we'll create the PoolableConnectionFactory, which wraps
        // the "real" Connections created by the ConnectionFactory with
        // the classes that implement the pooling functionality.
        //
        PoolableConnectionFactory poolableConnectionFactory =
                new PoolableConnectionFactory(connectionFactory, null);
        //
        // Now we'll need a ObjectPool that serves as the
        // actual pool of connections.
        //
        // We'll use a GenericObjectPool instance, although
        // any ObjectPool implementation will suffice.
        //
        GenericObjectPool<PoolableConnection> connectionPool =
                new GenericObjectPool<>(poolableConnectionFactory);
        connectionPool.setConfig(jdbcConfig);

        // Set the factory's pool property to the owning pool
        poolableConnectionFactory.setPool(connectionPool);
        //
        // Finally, we create the PoolingDriver itself,
        // passing in the object pool we created.
        //
        PoolingDataSource<PoolableConnection> dataSource =
                new PoolingDataSource<>(connectionPool);

        return dataSource;
    }


}
