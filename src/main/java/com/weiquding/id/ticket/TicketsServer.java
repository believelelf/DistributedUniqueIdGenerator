package com.weiquding.id.ticket;

import com.weiquding.id.db.PoolUtils;
import com.weiquding.id.exception.IDException;
import org.apache.commons.dbcp2.PoolableConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;

/**
 * ticketsServer方案
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/2
 */
public class TicketsServer {

    private static final String REPLACE_STATEMENT = "REPLACE INTO TICKETS64(STUB) VALUES(?)";

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketsServer.class);

    /**
     * 获得TicketServer实例
     *
     * @param dataSource 数据源
     * @return TicketServer实例
     */
    public static TicketsServer getInstance(DataSource dataSource) {
        if (dataSource == null) {
            throw new IllegalArgumentException("datasource must not be null.");
        }
        return new TicketsServer(dataSource);
    }

    /**
     * 默认stub-存根
     */
    private String stub = "ticket";

    /***
     * 重试次数
     */
    private int retryCount = 20;

    /**
     * 数据源
     */
    private DataSource dataSource;

    private TicketsServer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setStub(String stub) {
        this.stub = stub;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * 产生下一个ID
     *
     * @return 下一个ID
     */
    public long nextId() {
        return nextId(0);
    }

    /**
     * @param rc 当前重试次数
     * @return id
     * @see PoolableConnection#close()
     * // Normal close: underlying connection is still open, so we
     * // simply need to return this proxy to the pool
     * try {
     * pool.returnObject(this);
     * } catch (final IllegalStateException e) {
     * // pool is closed, so close the connection
     * passivate();
     * getInnermostDelegate().close();
     * } catch (final SQLException e) {
     * throw e;
     * } catch (final RuntimeException e) {
     * throw e;
     * } catch (final Exception e) {
     * throw new SQLException("Cannot close connection (return to pool failed)", e);
     * }
     * https://stackoverflow.com/questions/4938517/closing-jdbc-connections-in-pool
     */
    private long nextId(int rc) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;
        try {
            connection = dataSource.getConnection();
            preparedStatement = connection.prepareStatement(REPLACE_STATEMENT, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, stub);
            int executeUpdate = preparedStatement.executeUpdate();
            LOGGER.debug("execute sql:[{}] with parameters:[{}] ==> result:[{}]", REPLACE_STATEMENT, stub, executeUpdate);
            generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                long id = generatedKeys.getLong(1);
                LOGGER.debug("The generated id is [{}]", id);
                return id;
            } else {
                throw new IDException("An error occurred while generating the ID");
            }
        } catch (SQLException e) {
            LOGGER.warn("An error occurred while generating the ID: [{}]", e.getMessage());
            if (rc < retryCount) {
                return nextId(++rc);
            } else {
                throw new IDException("An error occurred while generating the ID", e);
            }
        } finally {
            PoolUtils.close(connection, preparedStatement, generatedKeys);
        }
    }
}
