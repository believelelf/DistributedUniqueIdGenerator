package com.weiquding.id.time.impl;

import com.weiquding.id.db.PoolUtils;
import com.weiquding.id.exception.IDException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 基于数据库时间作为远程时间服务器的实现
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/7
 */
public class DatabaseTimeServiceImpl extends SimpleTimeServiceImpl {

    /**
     * mysql> select current_timestamp;
     * +---------------------+
     * | current_timestamp   |
     * +---------------------+
     * | 2019-12-07 13:05:52 |
     * +---------------------+
     * 1 row in set (0.03 sec)
     */
    private String timestampSql = "select current_timestamp";

    private DataSource dataSource;

    public void setTimestampSql(String timestampSql) {
        this.timestampSql = timestampSql;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected long remoteTimestamp() {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(timestampSql);
            if (resultSet.next()) {
                return resultSet.getTimestamp(1).getTime();
            } else {
                throw new IDException("cannot get timestamp from database.");
            }
        } catch (SQLException e) {
            throw new IDException("cannot get timestamp from database.", e);
        } finally {
            PoolUtils.close(connection, statement, resultSet);
        }
    }
}
