package com.tianji.aigc.service;

import com.tianji.aigc.vo.ChatEventVO;
import reactor.core.publisher.Flux;

public interface ChatService {

    /**
     * 处理用户问题并返回流式响应
     * @param question 用户问题
     * @param sessionId 会话ID
     * @return Flux流式聊天事件响应
     */
    Flux<ChatEventVO> chat(String question, String sessionId);
}
