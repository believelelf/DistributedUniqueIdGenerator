package com.weiquding.id.ticket;

import com.weiquding.id.db.PoolUtils;
import com.weiquding.id.exception.IDException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * ticketsServer方案
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/2
 */
public class TicketsServer {

    /**
     * stub-类型
     */
    private static final String DEFAULT_STUB = "ticket";

    private static final int MAX_RETRY_COUNT = 3;

    private static final String REPLACE_STATEMENT = "REPLACE INTO TICKETS64(STUB) VALUES(?)";

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketsServer.class);

    /**
     * 产生下一个ID
     *
     * @return 下一个ID
     */
    public static long nextId() {
        return nextId(0);
    }

    private static long nextId(int retryCount) {
        Connection connection = PoolUtils.getConnection();
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;
        try {
            preparedStatement = connection.prepareStatement(REPLACE_STATEMENT, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, DEFAULT_STUB);
            int executeUpdate = preparedStatement.executeUpdate();
            LOGGER.debug("execute sql:[{}] with parameters:[{}] ==> result:[{}]", REPLACE_STATEMENT, DEFAULT_STUB, executeUpdate);
            generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                long id = generatedKeys.getLong(1);
                LOGGER.debug("The generated id is [{}]", id);
                return id;
            } else {
                throw new IDException("An error occurred while generating the ID");
            }
        } catch (SQLException e) {
            LOGGER.error("An error occurred while generating the ID: [{}]", e.getMessage());
            if (retryCount < MAX_RETRY_COUNT) {
                return nextId(++retryCount);
            }else{
                throw new IDException("An error occurred while generating the ID", e);
            }
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    LOGGER.error("An error occurred while closing PreparedStatement", e);
                }
            }
            if (generatedKeys != null) {
                try {
                    generatedKeys.close();
                } catch (SQLException e) {
                    LOGGER.error("An error occurred while closing ResultSet", e);
                }
            }
        }
    }
}
