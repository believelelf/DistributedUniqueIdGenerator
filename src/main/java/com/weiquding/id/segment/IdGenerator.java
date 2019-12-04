package com.weiquding.id.segment;

import java.util.List;

/**
 * 数据库批量段方案接口
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/3
 */
public interface IdGenerator<T> {

    /**
     * 生成单个ID
     *
     * @return ID
     */
    T generate();

    /**
     * 生成指定size个ID
     *
     * @param size 数量
     * @return size个ID
     */
    List<T> generate(int size);

}
