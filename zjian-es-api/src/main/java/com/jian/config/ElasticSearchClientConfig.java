package com.jian.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchClientConfig {
    // spring <bean id="restHighLevelClient" class="RestHighLevelClient"/>
    // 步骤：1：找对象  2：放到spring中待用
    // 3：如果是springBoot就i就先分析源码
    // Xxx AutoConfiguration  XxxProperties
    @Bean
    public RestHighLevelClient restHighLevelClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("127.0.0.1", 9200, "http")));
        return  client;
    }

}
