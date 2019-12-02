package com.weiquding.id.db;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PoolUtilsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PoolUtilsTest.class);

    @Test
    public void testPoolUtils() {
        try {
            Connection conn = PoolUtils.getConnection();
            Statement stmt = conn.createStatement();
            LOGGER.info("Executing statement.");
            ResultSet rset = stmt.executeQuery("select id from tickets64");
            LOGGER.info("Results:");
            int numcols = rset.getMetaData().getColumnCount();
            while (rset.next()) {
                for (int i = 1; i <= numcols; i++) {
                    LOGGER.info("\t" + rset.getString(i));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("An error occurred while executing SQL", e);
        }
    }
}
