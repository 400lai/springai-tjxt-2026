package com.tianji.aigc.memory;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.tianji.aigc.utils.MessageUtil;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Set;

/**
 * 基于Redis实现的ChatMemoryRepository
 */
public class RedisChatMemoryRepository implements ChatMemoryRepository {
    /** 默认Redis键前缀 */
    public static final String DEFAULT_PREFIX = "CHAT:";
    /** Redis键前缀，用于区分不同的业务场景 */
    private final String prefix;

    /** Spring Redis模板，用于执行Redis操作 */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /** 默认构造函数，使用默认的Redis键前缀"CHAT:" */
    public RedisChatMemoryRepository() {
        this.prefix = DEFAULT_PREFIX;
    }

    /** 带自定义前缀的构造函数 */
    public RedisChatMemoryRepository(String prefix) {
        this.prefix = prefix;
    }

    /** 查询所有会话ID列表 */
    @Override
    public List<String> findConversationIds() {
        // 从Redis中查询所有以prefix开头的键集合
        Set<String> keys = this.stringRedisTemplate.keys(this.prefix + "*");
        // 如果Redis返回null（可能Redis不可用），返回空列表避免NPE
        if (null == keys) {
            return List.of();
        }
        // 将键集合转换为流，移除prefix前缀提取sessionId，收集为列表
        return StreamUtil.of(keys)
                .map(key -> StrUtil.replace(key, this.prefix, ""))
                .toList();
    }

    /** 根据会话ID查询该会话的所有消息 */
    @Override
    public List<Message> findByConversationId(String conversationId) {
        // 生成Redis键名用于存储会话消息
        var redisKey = this.getKey(conversationId);
        // 获取Redis列表操作对象
        var listOps = this.stringRedisTemplate.boundListOps(redisKey);

        // 从Redis列表中获取所有的数据
        var messages = listOps.range(0, -1);
        // 将Redis返回的字符串列表转换为Message对象列表
        return CollStreamUtil.toList(messages, MessageUtil::toMessage);
    }

    /** 保存指定会话的完整消息列表到Redis */
    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        Assert.notEmpty(messages, "消息列表不能为空");
        var redisKey = this.getKey(conversationId);
        var listOps = this.stringRedisTemplate.boundListOps(redisKey);
        // 将原有消息全部删除
        this.deleteByConversationId(conversationId);
        // 将消息序列化并添加到Redis列表的右侧
        messages.forEach(message -> listOps.rightPush(MessageUtil.toJson(message)));
    }

    /** 删除指定会话的所有消息 */
    @Override
    public void deleteByConversationId(String conversationId) {
        var redisKey = this.getKey(conversationId);
        this.stringRedisTemplate.delete(redisKey);
    }

    /** 构建Redis键 */
    private String getKey(String conversationId) {
        return prefix + conversationId;
    }
}
