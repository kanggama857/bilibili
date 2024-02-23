package cn.laiyuejia.bilibili.service.impl;

import cn.laiyuejia.bilibili.domain.UserInfo;
import cn.laiyuejia.bilibili.domain.Video;
import cn.laiyuejia.bilibili.repository.UserInfoRepository;
import cn.laiyuejia.bilibili.repository.VideoRepository;
import cn.laiyuejia.bilibili.service.ElasticSearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public void addVideo(Video video){
        videoRepository.save(video);
    }

    @Override
    public Video getVideo(String keyword){
        return videoRepository.findByTitleLike(keyword);
    }

    @Override
    public void deleteAllVideos(){
        videoRepository.deleteAll();
    }

    @Override
    public void addUserInfo(UserInfo userInfo){
        userInfoRepository.save(userInfo);
    }

    @Override
    public void deleteAllUserInfos(){
        userInfoRepository.deleteAll();
    }

    @Override
    public List<Map<String,Object>> getContents(String keyword, Integer pageNumber, Integer pageSize) throws IOException {
        String[] indexes = {"videos","userinfos"};
        // 指定要搜索的索引名称
        SearchRequest searchRequest = new SearchRequest(indexes);
        // 用于构建搜索请求的查询条件。设置每页大小和起始页码
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(pageSize);
        searchSourceBuilder.from(pageNumber-1);
        // 用于构建多字段的匹配查询。设置要匹配的关键字和需要匹配的字段
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(keyword,"title","nick","description");
        searchSourceBuilder.query(multiMatchQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        // 设置搜索请求的超时时间为60秒
        searchSourceBuilder.timeout(new TimeValue(60,TimeUnit.SECONDS));
        //高亮显示
        String[] array = {"title","nick","description"};
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        for(String key:array){
            highlightBuilder.fields().add(new HighlightBuilder.Field(key));
        }
        // 配置高亮显示的相关参数，包括是否要求字段完全匹配、前置标签和后置标签
        highlightBuilder.requireFieldMatch(false); //多个字段高亮 false
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        //执行搜索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<Map<String,Object>> arrayList = new ArrayList<>();
        for(SearchHit hit :searchResponse.getHits()){
            //处理高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            Map<String,Object> sourceMap = hit.getSourceAsMap();
            for(String key :array){
                HighlightField highlightField = highlightFields.get(key);
                if(highlightField !=null){
                    Text[] fragments = highlightField.getFragments();
                    String str = Arrays.toString(fragments);
                    str = str.substring(1,str.length()-1);
                    sourceMap.put(key,str);
                }
            }
            arrayList.add(sourceMap);
        }
        return arrayList;
    }
}
