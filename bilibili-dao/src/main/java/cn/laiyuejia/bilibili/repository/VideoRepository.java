package cn.laiyuejia.bilibili.repository;


import cn.laiyuejia.bilibili.domain.Video;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
// spring-data 帮助连接es
@Repository
public interface VideoRepository extends ElasticsearchRepository<Video,Long> {
    // spring-data 对这种命名格式会进行拆解 -> find by title like
    Video findByTitleLike(String keyword);
}
