package com.li.socialplatform.config;

import com.li.socialplatform.store.InMongoChatMemoryStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MemoryChatConfig {

    private final InMongoChatMemoryStore inMongoChatMemoryStore;

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory
                .builder()
                .id(memoryId) // 每个会话的记忆id
                .maxMessages(20) // 存储最近20条消息
                .chatMemoryStore(inMongoChatMemoryStore) // 使用自定义的存储 mongodb
                .build();
    }
}
