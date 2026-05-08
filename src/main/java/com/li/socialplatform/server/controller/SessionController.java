package com.li.socialplatform.server.controller;

import com.li.socialplatform.pojo.entity.Result;
import com.li.socialplatform.server.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    // 新建会话
    @GetMapping
    public Result create() {
        return sessionService.create();
    }

    // 删除会话
    @DeleteMapping("/{sessionId}")
    public Result delete(@PathVariable("sessionId") String sessionId) {
        return sessionService.delete(sessionId);
    }

    // 获取所有会话
    @GetMapping("/all")
    public Result getAll(@RequestParam(defaultValue = "1", required = false, name = "page") Integer page,
                         @RequestParam(defaultValue = "10", required = false, name = "size") Integer size) {
        return sessionService.getSessions(page, size);
    }

    // 获取指定会话记录
    @GetMapping("/{sessionId}")
    public Result getSession(@PathVariable("sessionId") String sessionId) {
        return sessionService.getSession(sessionId);
    }
}
