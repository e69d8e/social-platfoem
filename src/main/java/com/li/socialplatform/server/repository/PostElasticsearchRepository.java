package com.li.socialplatform.server.repository;

import com.li.socialplatform.pojo.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface PostElasticsearchRepository extends ElasticsearchRepository<Post, Long> {
    List<Post> findByTitleOrContentAndCategoryId(String title, String content, Integer categoryId, Pageable pageable);
    Long countByTitleOrContentAndCategoryId(String title, String content, Integer categoryId);
    List<Post> findByCategoryId(Integer categoryId, Pageable pageable);
    Long countByCategoryId(Integer categoryId);
    List<Post> findByTitleOrContent(String title, String content, Pageable pageable);
    Long countByTitleOrContent(String title, String content);
}
