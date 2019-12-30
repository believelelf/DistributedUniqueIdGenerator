package com.weiquding.id.segment.generator;

import com.weiquding.id.segment.IdGenerator;
import com.weiquding.id.segment.domain.IdResult;
import com.weiquding.id.segment.format.IdFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * ID生成工厂,实现ID格式化输出。
 *
 * @author wubaiyi
 * @version V1.0
 * @date 2019/12/4
 */
public abstract class AbstractIdGenerator implements IdGenerator<String> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * 业务类型
     */
    protected String bizType;
    /**
     * ID格式化
     */
    private IdFormat idFormat;

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public void setFormat(String pattern) {
        this.idFormat = IdFormat.getInstance(pattern);
    }


    @Override
    public String generate() {
        return generate(1).get(0);
    }


    @Override
    public List<String> generate(int count) {
        if (bizType == null) {
            throw new IllegalArgumentException("bizType must not be null.");
        }
        // 子类实现
        IdResult idResult = internalGenerate(count);
        List<String> ids = new ArrayList<>(10);
        if (idFormat == null) {
            for (int i = 0; i < count; i++) {
                ids.add(Long.toString(idResult.getId() + i));
            }
        } else {
            for (int i = 0; i < count; i++) {
                ids.add(idFormat.format(this, idResult.getId() + i, idResult.getGenerateTime()));
            }
        }
        return ids;
    }

    /**
     * ID生成方法
     *
     * @param count 生成ID个数
     * @return 生成ID的集合
     */
    protected abstract IdResult internalGenerate(int count);
}
