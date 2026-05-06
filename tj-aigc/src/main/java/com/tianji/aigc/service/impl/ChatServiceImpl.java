package com.tianji.aigc.service.impl;

import cn.hutool.core.date.DateUtil;
import com.tianji.aigc.config.SystemPromptConfig;
import com.tianji.aigc.enums.ChatEventTypeEnum;
import com.tianji.aigc.service.ChatService;
import com.tianji.aigc.vo.ChatEventVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI聊天服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    /** Spring AI聊天客户端，用于与大模型进行交互 */
    private final ChatClient chatClient;
    /** 系统提示词配置，定义AI助手的行为和角色 */
    private final SystemPromptConfig systemPromptConfig;
    /** 聊天记忆对象，用于管理和持久化对话历史 */
    private final ChatMemory chatMemory;

    /**
     * 通过一个容器，保存当前会话的会话id 以及 是否继续生成的标识，用于后续停止会话
     * 容器实现：
     * 1、使用Map(采用ConcurrentHashMap是确保线程安全)
     * 2、考虑到分布式场景，需要使用redis
     */
    // private static final Map<String, Boolean> GENERATE_STATUS = new ConcurrentHashMap<>();

    /** Redis模板对象，用于管理会话的生成状态 */
    private final StringRedisTemplate stringRedisTemplate;
    /** Redis中存储生成状态的Hash键名 */
    private static final String GENERATE_STATUS_KEY = "GENERATE_STATUS";

    /** 处理用户问题并返回流式响应 */
    @Override
    public Flux<ChatEventVO> chat(String question, String sessionId) {
        // 将会话id转换为对话id
        var conversationId = ChatService.getConversationId(sessionId);
        // 大模型输出内容的缓存器，用于在输出中断后的数据存储
        var outputBuilder = new StringBuilder();
        var hashOps = stringRedisTemplate.boundHashOps(GENERATE_STATUS_KEY);

        return this.chatClient.prompt()
                .system(promptSystem -> promptSystem
                        .text(this.systemPromptConfig.getChatSystemMessage().get()) // 设置系统提示语
                        .param("now", DateUtil.now()) // 设置当前时间的参数
                )
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(question)
                .stream()
                .chatResponse() // 获取大模型返回的流式响应（Flux<ChatResponse>）
                .doFirst(() -> hashOps.put(sessionId, "true")) // 第一次输出内容时执行
                .doOnError(throwable -> hashOps.delete(sessionId)) // 出现异常时，删除标识
                .doOnCancel(() -> {
                    // 当输出被取消时，保存输出的内容到历史记录中
                    this.saveStopHistoryRecord(conversationId, outputBuilder.toString());
                })
                .doOnComplete(() -> hashOps.delete(sessionId)) // 完成时执行，删除标识
                .takeWhile(response -> { // 通过返回值来控制Flux流是否继续，true：继续，false：终止
                    return hashOps.get(sessionId) != null;
                })
                .map(chatResponse -> {
                    // 获取大模型的输出的文本片段内容
                    String text = chatResponse.getResult().getOutput().getText();
                    // 追加到输出内容中
                    outputBuilder.append(text);
                    // 封装响应对象
                    return ChatEventVO.builder()
                            .eventData(text)
                            .eventType(ChatEventTypeEnum.DATA.getValue())
                            .build();
                })
                .concatWith(Flux.just(ChatEventVO.builder()  // 标记输出结束
                        .eventType(ChatEventTypeEnum.STOP.getValue())
                        .build()));
    }

    /**
     * 保存中断时的对话记录到聊天记忆
     * @param conversationId 会话的唯一标识符
     * @param content        大模型已输出的内容片段
     */
    private void saveStopHistoryRecord(String conversationId, String content) {
        this.chatMemory.add(conversationId, new AssistantMessage(content));
    }

    /** 移除会话的生成状态标记，终止流式响应 */
    @Override
    public void stop(String sessionId) {
        var hashOps = stringRedisTemplate.boundHashOps(GENERATE_STATUS_KEY);
        hashOps.delete(sessionId);
    }
}
