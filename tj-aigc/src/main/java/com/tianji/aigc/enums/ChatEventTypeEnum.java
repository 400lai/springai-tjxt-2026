package com.tianji.aigc.enums;

import com.tianji.common.enums.BaseEnum;
import lombok.Getter;

/**
 * 聊天消息事件类型
 */
@Getter
public enum ChatEventTypeEnum implements BaseEnum {
    /** 数据事件，用于传输实际的聊天内容或响应数据 */
    DATA(1001, "数据事件"),
    /** 停止事件，标识聊天响应的结束或中断 */
    STOP(1002, "停止事件"),
    /** 参数事件，用于传递配置参数或元数据信息 */
    PARAM(1003, "参数事件");

    /** 事件类型的数值标识 */
    private final int value;
    /** 事件类型的描述信息 */
    private final String desc;

    /** 构造函数 */
    ChatEventTypeEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    /** 重写toString方法，返回枚举名称 */
    @Override
    public String toString() {
        return this.name();
    }
}
