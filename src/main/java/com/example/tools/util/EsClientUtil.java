package com.example.tools.util;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

public class EsClientUtil {
    public static RestHighLevelClient getLssEsClient() {
        return new RestHighLevelClient(RestClient.builder(new HttpHost("10.169.44.152", 80, "http"))
                    .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                                .setConnectTimeout(30000)
                                .setSocketTimeout(30000)));
    }
}
