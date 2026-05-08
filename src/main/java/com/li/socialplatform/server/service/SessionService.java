package com.li.socialplatform.server.service;


import com.li.socialplatform.pojo.entity.Result;

public interface SessionService {
    Result create();

    Result delete(String sessionId);

    Result getSessions(Integer page, Integer size);

    Result getSession(String sessionId);
}
