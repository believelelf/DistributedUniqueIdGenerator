package com.weiquding.id.segment.domain;

/**
 * ID加载状态
 *
 * @author beliveyourself
 * @version V1.0
 * @date 2019/12/30
 */
public enum  IdState {

    /**
     * 正常可用
     */
    NORMAL,
    /**
     * 需要预加载
     */
    LOADING,
    /**
     *ID超过maxId,不可用
     */
    OVER
}
