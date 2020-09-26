package com.jian.jianesjd.service;

import com.alibaba.fastjson.JSON;
import com.jian.jianesjd.pojo.Content;
import com.jian.jianesjd.utils.HTMLParseUtil;
import lombok.SneakyThrows;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ContentService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;
// 解析数据放入ES索引中
    public Boolean parseContent(String keywords) throws IOException {
        List<Content> contents = new HTMLParseUtil().parse(keywords);
        // 把查询到的数据放入ES中
        BulkRequest goods = new BulkRequest("jd_goods");
        goods.timeout("2m");
        for (int i = 0; i < contents.size(); i++) {
            goods.add(new IndexRequest("jd_goods").source(JSON.toJSONString(contents.get(i)), XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(goods, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }
//不能直接使用 @Autowired 只能在Spring容器中起作用
   /* public static void main(String[] args) throws IOException {
        new ContentService().parseContent("java");
    }*/

   //2.获取这些数据实现搜索功能
    @SneakyThrows
    public List<Map<String ,Object>> searchHighLighter(String keywords,int pageNo,int pageSize){
        if (pageNo < 1){
            pageNo=1;
        }
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        //精准匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keywords);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        // 执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //解析结果
        List<Map<String ,Object>> list = new ArrayList<>();
        for (SearchHit hit : search.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            list.add(sourceAsMap);
        }
        return list;
    }

    //2.获取这些数据实现搜索功能,实现高亮
    @SneakyThrows
    public List<Map<String ,Object>> searchPage(String keywords,int pageNo,int pageSize){
        if (pageNo < 1){
            pageNo=1;
        }
        //条件搜索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //分页
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        //精准匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keywords);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.requireFieldMatch(true);   //多个字段高亮设置
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);

        // 执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //解析结果
        List<Map<String ,Object>> list = new ArrayList<>();
        for (SearchHit hit : search.getHits().getHits()) {
            // 获取高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();  //原来的结果
            //解析高亮字段,将原来的字段换成高亮字段
            if(title!=null){
                Text[] fragments = title.fragments();
                //将高亮字段替换没有高亮的字段
                String n_title="";
                for (Text fragment : fragments) {
                    n_title +=fragment;
                }
                sourceAsMap.put("title",n_title);  //高亮字段替换掉原来的字段即可
            }
            list.add(sourceAsMap);
        }
        return list;
    }
}
