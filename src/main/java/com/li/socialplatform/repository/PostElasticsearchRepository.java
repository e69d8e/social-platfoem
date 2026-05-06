package com.li.socialplatform.repository;

import com.li.socialplatform.pojo.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface PostElasticsearchRepository extends ElasticsearchRepository<Post, Long> {
    List<Post> findByTitleOrContent(String title, String content, Pageable pageable);
}
