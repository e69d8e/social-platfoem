package com.li.socialplatform.server.task;

import com.li.socialplatform.common.constant.KeyConstant;
import com.li.socialplatform.pojo.entity.Post;
import com.li.socialplatform.server.mapper.PostMapper;
import com.li.socialplatform.server.repository.PostElasticsearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author e69d8e
 * @since 2026/05/23
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ViewCountSyncTask {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PostMapper postMapper;
    private final PostElasticsearchRepository postElasticsearchRepository;

    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void syncViewCounts() {
        Set<String> keys = redisTemplate.keys(KeyConstant.POST_VIEW_COUNT + "*");
        if (keys.isEmpty()) {
            return;
        }
        log.info("开始同步帖子浏览量, 共 {} 条", keys.size());
        int synced = 0;
        for (String key : keys) {
            try {
                Integer count = (Integer) redisTemplate.opsForValue().get(key);
                if (count == null || count <= 0) {
                    redisTemplate.delete(key);
                    continue;
                }
                long postId = Long.parseLong(key.substring(KeyConstant.POST_VIEW_COUNT.length()));
                Post post = postMapper.selectById(postId);
                if (post == null) {
                    redisTemplate.delete(key);
                    continue;
                }
                int currentDbCount = post.getViewCount() == null ? 0 : post.getViewCount();
                post.setViewCount(currentDbCount + count);
                postMapper.updateById(post);
                postElasticsearchRepository.save(post);
                redisTemplate.delete(key);
                synced++;
            } catch (Exception e) {
                log.error("同步帖子浏览量失败: key={}, error={}", key, e.getMessage());
            }
        }
        log.info("帖子浏览量同步完成, 成功同步 {} 条", synced);
    }
}
