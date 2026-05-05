package com.tianji.aigc.enums;

import com.tianji.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 消息类型枚举
 */
@Getter
public enum MessageTypeEnum implements BaseEnum {
    /** 用户发送的提问消息 */
    USER(1, "用户提问"),
    /** AI助手返回的回答消息 */
    ASSISTANT(2, "AI的回答");

    /** 消息类型的数值标识 */
    private final int value;
    /** 消息类型的描述信息 */
    private final String desc;

    /** 构造函数 */
    MessageTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /** 重写toString方法，返回枚举名称 */
    @Override
    public String toString() {
        return this.name();
    }
}