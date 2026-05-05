package com.tianji.aigc.memory;

import lombok.Data;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.content.Media;

import java.util.List;
import java.util.Map;

/**
 * 自定义消息数据结构：封装与大模型交互的消息内容，包含消息类型、文本内容、媒体信息
 */
@Data
public class MyMessage {
    /** 消息类型，用于标识消息的类别（如用户消息、助手消息、系统消息等） */
    private String messageType;
    /** 消息元数据，存储额外的上下文信息和属性 */
    private Map<String, Object> metadata = Map.of();
    /** 媒体内容列表，支持图片、音频等多媒体资源 */
    private List<Media> media = List.of();
    /** 工具调用列表，记录需要调用的外部工具或函数 */
    private List<AssistantMessage.ToolCall> toolCalls = List.of();
    /** 文本内容，消息的主要文本信息 */
    private String textContent;
    /** 工具响应列表，存储工具调用的返回结果 */
    private List<ToolResponseMessage.ToolResponse> toolResponses = List.of();

    /** 自定义参数字段，用于扩展消息的附加信息 */
    private Map<String, Object> params = Map.of();
}
