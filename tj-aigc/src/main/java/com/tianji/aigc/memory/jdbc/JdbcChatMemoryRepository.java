package com.tianji.aigc.memory.jdbc;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianji.aigc.entity.ChatRecord;
import com.tianji.aigc.service.ChatRecordService;
import com.tianji.aigc.utils.MessageUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 基于 JDBC 的聊天记忆存储库实现类
 */
public class JdbcChatMemoryRepository implements ChatMemoryRepository {
    @Resource
    private ChatRecordService chatRecordService;

    /** 查找所有已保存的对话 ID 列表 */
    @Override
    public List<String> findConversationIds() {
        // 查询所有对话记录的 conversation_id 字段
        var chatRecordList = this.chatRecordService.lambdaQuery()
                .select(ChatRecord::getConversationId)
                .list();
        // 提取并转换结果集为对话ID列表
        return CollStreamUtil.toList(chatRecordList, ChatRecord::getConversationId);
    }

    /** 根据对话 ID 查找对应的消息记录列表 */
    @Override
    public List<Message> findByConversationId(String conversationId) {
        // 根据对话 ID 查询记录并按时间升序排列
        var chatRecordList = this.chatRecordService.lambdaQuery()
                .eq(ChatRecord::getConversationId, conversationId)
                .orderByAsc(ChatRecord::getCreateTime)
                .list();
        // 转换数据格式为 Message 对象列表
        return CollStreamUtil.toList(chatRecordList, chatRecord -> MessageUtil.toMessage(chatRecord.getData()));
    }

    /** 批量保存消息到指定对话中 */
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        // 删除该对话ID下的所有记录
        this.deleteByConversationId(conversationId);
        // 通过对话id获取用户id
        var userId = Convert.toLong(StrUtil.subBefore(conversationId, "_", false));
        // 批量保存消息记录到数据库
        var chatRecordList = CollStreamUtil.toList(messages, message -> ChatRecord.builder()
                .conversationId(conversationId)
                .data(MessageUtil.toJson(message))
                .creater(userId)
                .updater(userId)
                .build());
        this.chatRecordService.saveBatch(chatRecordList);
    }

    /** 删除指定对话下的所有消息记录 */
    @Override
    public void deleteByConversationId(String conversationId) {
        var queryWrapper = Wrappers.<ChatRecord>lambdaQuery()
                .eq(ChatRecord::getConversationId, conversationId);
        this.chatRecordService.remove(queryWrapper);
    }
}