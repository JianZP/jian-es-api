package com.jian;

import com.alibaba.fastjson.JSON;
import com.jian.pojo.User;
import org.apache.lucene.search.TermQuery;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.OptionalDataException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 所有api详解 7.9.0高级客户端api测试
 */
@SpringBootTest
class ZjianEsApiApplicationTests {
    // 通过面向对象来进行操作
    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;
    //测试索引的创建  request  PUT jian_index
    @Test
    void contextLoads() throws IOException {
        // 1.创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("jian_index");
        // 2.执行创建索引请求  IndicesClient，请求后获得响应
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);
    }
    //测试 获取索引
    @Test
    void testExitIndex() throws IOException {
        GetIndexRequest getIndexRequest = new GetIndexRequest("jian_index");
        boolean exists = client.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
    //测试删除索引
    @Test
    void testDeleteIndex() throws IOException {
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("jian_index");
        AcknowledgedResponse delete = client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
        System.out.println(delete.isAcknowledged());

    }
    // 测试添加文档
    @Test
    void testAddDocument() throws IOException {
        // 1.创建对象
        User user = new User("健",18);
        // 2.创建请求
        IndexRequest index = new IndexRequest("jian_index");
        // 3.创建规则 PUT /jian_index/_doc/1
        index.id("1");
        index.timeout(TimeValue.timeValueSeconds(1));
        // index.timeout("1s");
        // 4.将数据放入请求  JSON数据
        IndexRequest source = index.source(JSON.toJSONString(user), XContentType.JSON);
        // 5.客户端发送请求  获取响应结果
        IndexResponse response = client.index(index, RequestOptions.DEFAULT);
        System.out.println(response.toString());
        System.out.println(response.status());   //对应命令返回的状态
    }
    // 获取文档  先判断是否存在 GET /index/_doc/1
    @Test
    void testIsExists() throws IOException {
        GetRequest request = new GetRequest("jian_index", "1");
        // 不获取返回的 _source 的上下文了
        //request.fetchSourceContext(new FetchSourceContext(false));
        //request.storedFields("_none_");
        boolean exists = client.exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
        if (exists == true){
            // 获取文档信息
            GetResponse documentFields = client.get(request, RequestOptions.DEFAULT);
            System.out.println(documentFields.getSourceAsString()); //打印文档内容
            System.out.println(documentFields);
        }
    }
    // 更新文档记录
    @Test
    void testUpdateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("jian_index","1");
        request.timeout("1s");
        User user = new User("健健",20);
        request.doc(JSON.toJSONString(user),XContentType.JSON);
        UpdateResponse update = client.update(request, RequestOptions.DEFAULT);
        System.out.println(update);
    }
    // 删除文档记录
    @Test
    void testDeleteDocument() throws IOException {
        DeleteRequest index = new DeleteRequest("jian_index", "1");
        index.timeout("1s");
        DeleteResponse response = client.delete(index,RequestOptions.DEFAULT);
        System.out.println(response.status());
    }

    // 批量插入数据
    @Test
    void testBulkInsertDocument() throws IOException {
        BulkRequest request = new BulkRequest();
        request.timeout("10s");
        List<User> users=new ArrayList<>();
        users.add(new User("健1",18));
        users.add(new User("健2",18));
        users.add(new User("健3",18));
        users.add(new User("健4",18));
        users.add(new User("健5",18));
        users.add(new User("健6",18));
        //批量处理请求
        for (int i = 0; i < users.size(); i++) {
            request.add(
                    new IndexRequest("jian_index")
                            .id(""+(i+1))
                            .source(JSON.toJSONString(users.get(i)),XContentType.JSON));
        }
        BulkResponse bulk = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println(bulk);
        System.out.println(bulk.status());
    }
    // 搜索
    @Test
    void testSearch() throws IOException {
        // 搜索请求
        SearchRequest searchRequest = new SearchRequest("jian_index");
        // 构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // sourceBuilder.highlighter() 高亮
        // 查询条件  使用 QueryBuilder 工具来实现
        // QueryBuilder.termQuery 精确匹配
        // QueryBuilder.matchAllQuery 匹配所有
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("age", 18);
        // MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);
        SearchResponse search = client.search(searchRequest, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(search.getHits()));
        System.out.println("+++++++++++++++++++++++++++++++++++++++");
        for (SearchHit documentFields : search.getHits().getHits()){
            System.out.println(documentFields.getSourceAsMap());
        }
    }
}
