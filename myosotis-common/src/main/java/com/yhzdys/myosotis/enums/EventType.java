package com.yhzdys.myosotis.enums;

/**
 * 配置变更类型
 */
public enum EventType {
    /**
     * 新增
     */
    ADD,
    /**
     * 更新
     */
    UPDATE,
    /**
     * 删除
     */
    DELETE,
    /**
     * 灰度 服务端使用，客户端不感知
     */
    GRAY,
}
