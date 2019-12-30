package com.weiquding.id.segment.generator.impl;

import com.weiquding.id.db.PoolUtils;
import com.weiquding.id.segment.domain.IdResult;
import com.weiquding.id.time.TimeService;
import com.weiquding.id.time.impl.DatabaseTimeServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;

/**
 * ID号段生成表
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/7
 */
public class IdTable {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdTable.class);

    private DataSource dataSource;
    private String tableName = "seq_no";
    private String bizTypeColName = "biz_type";
    private String maxIdColName = "max_id";
    private String descriptionColName = "description";
    private String updateTimeColName = "update_time";

    private String insertSql;
    private String updateSql;
    private String selectSql;
    private String resetSql;

    private TimeService timeService = new DatabaseTimeServiceImpl();
    private String isolationFlag;

    //******************setter and getter****************//

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setBizTypeColName(String bizTypeColName) {
        this.bizTypeColName = bizTypeColName;
    }

    public void setMaxIdColName(String maxIdColName) {
        this.maxIdColName = maxIdColName;
    }

    public void setDescriptionColName(String descriptionColName) {
        this.descriptionColName = descriptionColName;
    }

    public void setUpdateTimeColName(String updateTimeColName) {
        this.updateTimeColName = updateTimeColName;
    }

    public void setInsertSql(String insertSql) {
        this.insertSql = insertSql;
    }

    public void setUpdateSql(String updateSql) {
        this.updateSql = updateSql;
    }

    public void setSelectSql(String selectSql) {
        this.selectSql = selectSql;
    }

    public void setResetSql(String resetSql) {
        this.resetSql = resetSql;
    }

    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    public void setIsolationFlag(String isolationFlag) {
        this.isolationFlag = isolationFlag;
    }

    public TimeService getTimeService() {
        return timeService;
    }

    protected Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }

    /**
     * 查询当前ID
     *
     * @param connection 连接
     * @param bizType    业务类型
     * @param dateCutoff 是否执行切换
     * @param cutoffType 切换类型
     * @return 当前ID
     */
    protected IdResult doSelect(Connection connection, String bizType, boolean dateCutoff, int cutoffType) throws SQLException {
        // 先从Timeserver取得时间
        long now = timeService.currentTimeMillis();
        IdResult idResult = new IdResult();
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = connection.prepareStatement(getSelectSql());
            ps.setString(1, bizType);
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                long id = resultSet.getLong(1);
                Timestamp ts = resultSet.getTimestamp(2);
                Timestamp generateTime = ts;
                // 纠正时间偏移的异常情况
                if (generateTime == null || generateTime.getTime() < now) {
                    generateTime = new Timestamp(now);
                }
                // 处理切换的情况
                if (dateCutoff) {
                    if (ts != null && timeService.isCutoff(cutoffType, now, ts.getTime())) {
                        // 需要进行切换
                        int affectRows = doReset(connection, bizType, now, id);
                        if (affectRows == 1) {
                            id = 1L;
                        }
                    } else {
                        // 再次默认查询切换类型为日切的情况
                        return doSelect(connection, bizType, dateCutoff);
                    }
                }
                idResult.setId(id);
                idResult.setGenerateTime(generateTime);
            } else {
                idResult.setGenerateTime(new Timestamp(now));
                LOGGER.warn("cannot get a id due to no initialized");
            }
        }finally {
            PoolUtils.close(null, ps, resultSet);
        }
        return idResult;
    }

    /**
     * 查询当前ID,切换类型为日切
     *
     * @param connection 连接
     * @param bizType    业务类型
     * @param dateCutoff 是否执行切换
     * @return 当前ID
     */
    protected IdResult doSelect(Connection connection, String bizType, boolean dateCutoff) throws SQLException {
        return doSelect(connection, bizType, dateCutoff, TimeService.DAY);
    }

    /**
     * 执行切换
     *
     * @param connection 连接
     * @param bizType    业务类型
     * @param now        当前时间
     * @param id         当前ID
     * @return 影响的行数
     */
    protected int doReset(Connection connection, String bizType, long now, long id) throws SQLException {
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = connection.prepareStatement(getResetSql());
            ps.setTimestamp(1, new Timestamp(now));
            ps.setString(2, bizType);
            ps.setLong(3, id);
            return ps.executeUpdate();
        }finally {
            PoolUtils.close(null, ps, resultSet);
        }
    }


    /**
     * 更新ID生成表
     *
     * @param connection 连接
     * @param bizType    业务类型
     * @param count      id个数
     * @param now        当前时间
     * @param id         前一ID
     * @return 影响行数
     */
    protected int doUpdate(Connection connection, String bizType, int count, long now, long id) throws SQLException {
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = connection.prepareStatement(getUpdateSql());
            ps.setLong(1, count);
            ps.setTimestamp(2, new Timestamp(now));
            ps.setString(3, bizType);
            ps.setLong(4, id);
            return ps.executeUpdate();
        }finally {
            PoolUtils.close(null, ps, resultSet);
        }

    }

    /**
     * 执行ID生成语句插入
     *
     * @param connection  数据库连接
     * @param bizType     业务类型
     * @param id          起始ID
     * @param now         当前时间
     * @param description 业务描述
     * @return 影响行数
     */
    protected int doInsert(Connection connection, String bizType, long id, long now, String description) throws SQLException {
        PreparedStatement ps = null;
        ResultSet resultSet = null;
        try {
            ps = connection.prepareStatement(getInsertSql());
            ps.setString(1, bizType);
            ps.setLong(2, id);
            ps.setTimestamp(3, new Timestamp(now));
            ps.setString(4, description);
            return ps.executeUpdate();
        } finally {
            PoolUtils.close(null, ps, resultSet);
        }
    }


    //*******************sql *****************//

    private String getSelectSql() {
        if (this.selectSql == null) {
            String _select = "select " + this.maxIdColName + ", " +
                    this.updateTimeColName +
                    " from " + this.tableName +
                    " where " + this.bizTypeColName + "=?" + " for update";
            if (this.isolationFlag != null) {
                _select += " with " + this.isolationFlag;
            }
            this.selectSql = _select;
        }
        return this.selectSql;
    }

    private String getResetSql() {
        if (this.resetSql == null) {
            String _reset = "update " + this.tableName +
                    " set " + this.maxIdColName + "  = 1, " +
                    this.updateTimeColName + "  = ?, " +
                    " where " + this.bizTypeColName + " = ? " +
                    " and " + this.maxIdColName + " = ?";
            this.resetSql = _reset;
        }
        return this.resetSql;
    }

    private String getUpdateSql() {
        if (this.updateSql == null) {
            String _update = "update " + this.tableName +
                    " set " + this.maxIdColName + " = " + this.maxIdColName + "+?, " +
                    this.updateTimeColName + "  = ?, " +
                    " where " + this.bizTypeColName + " = ? " +
                    " and " + this.maxIdColName + " = ?";
            this.updateSql = _update;
        }
        return this.updateSql;
    }

    private String getInsertSql() {
        if (this.insertSql == null) {
            String _insert = "insert into " + this.tableName +
                    " (" +
                    this.bizTypeColName + "," +
                    this.maxIdColName + ", " +
                    this.updateTimeColName + ", " +
                    this.descriptionColName +
                    ") values (?,?,?,?)";
            this.insertSql = _insert;
        }
        return this.insertSql;
    }

}
