package com.li.socialplatform.server.service;

import com.li.socialplatform.pojo.dto.UserMessageDTO;
import reactor.core.publisher.Flux;

public interface ChatService {
    Flux<String> chat(UserMessageDTO userMessageDTO);
}
