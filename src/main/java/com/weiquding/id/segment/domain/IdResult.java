package com.weiquding.id.segment.domain;

import java.util.Date;
import java.util.Objects;

/**
 * ID生成结果
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/5
 */
public class IdResult {

    /**
     * 生成时间
     */
    private Date generateTime;

    /**
     * 生成的Id
     */
    private long id = -1L;

    public Date getGenerateTime() {
        return generateTime;
    }

    public void setGenerateTime(Date generateTime) {
        this.generateTime = generateTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdResult idResult = (IdResult) o;
        return Objects.equals(generateTime, idResult.generateTime) &&
                id == idResult.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(generateTime, id);
    }

    @Override
    public String toString() {
        return "IdResult{" +
                "generateTime=" + generateTime +
                ", id=" + id +
                '}';
    }
}
