package com.weiquding.id.segment.generator.impl;

import com.weiquding.id.db.PoolUtils;
import com.weiquding.id.segment.domain.IdResult;
import com.weiquding.id.segment.generator.AbstractIdGenerator;
import com.weiquding.id.time.TimeService;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 基于数据库表批量取号功能的ID生成器
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/7
 */
public class TableIdGeneratorImpl extends AbstractIdGenerator {

    private IdTable idTable;
    private int retryCount = 20;
    private boolean dateCutoff;
    private boolean autoCommit = true;
    private String description;

    protected int cutoffType = TimeService.DAY;


    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setIdTable(IdTable idTable) {
        this.idTable = idTable;
    }

    public void setDateCutoff(boolean dateCutoff) {
        this.dateCutoff = dateCutoff;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public void setCutoffType(int cutoffType) {
        this.cutoffType = cutoffType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    protected IdResult internalGenerate(int count) {
        Connection connection = null;
        try {
            connection = idTable.getConnection();
            // 判断是否autoCommit
            boolean oldAutoCommit = connection.getAutoCommit();
            try {
                if (!autoCommit) {
                    connection.setAutoCommit(false);
                }
                int affectRows = 0;
                IdResult idResult = null;
                for (int i = 0; i < retryCount && affectRows != 1; i++) {
                    try {
                        idResult = idTable.doSelect(connection, bizType, dateCutoff, cutoffType);
                        // 未插入数据
                        if (idResult.getId() == -1L) {
                            idTable.doInsert(connection, bizType, 1L, idResult.getGenerateTime().getTime(), description);
                            idResult.setId(1L);
                        }
                        // 更新ID号段
                        affectRows = idTable.doUpdate(connection, bizType, count, idResult.getGenerateTime().getTime(), idResult.getId());
                    } catch (SQLException e) {
                        logger.warn("get id retry:{} cause:{}", i, e);
                        if (i == this.retryCount - 1) {
                            throw e;
                        }
                    }
                }
                if (affectRows != 1) {
                    throw new IllegalStateException("cannot get id, rows = " + affectRows);
                }
                if (!this.autoCommit) {
                    connection.commit();
                }
                return idResult;
            } catch (SQLException e) {
                if (!autoCommit) {
                    connection.rollback();
                }
                throw e;
            } finally {
                if (!this.autoCommit) {
                    connection.setAutoCommit(oldAutoCommit);
                }
                PoolUtils.close(connection, null, null);
            }
        }catch (SQLException e){
            throw new IllegalStateException(e);
        }
    }
}
