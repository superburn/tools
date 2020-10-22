package com.example.tools.controller;

import com.alibaba.fastjson.JSON;
import com.example.tools.service.EsService;
import com.example.tools.util.EsClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping(value = "/test")
@Slf4j
public class LssLogAnalyseController {
    @Autowired
    private EsService esService;

    @GetMapping(value = "/es")
    public void testEs() {
        try {
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.from(0);
            sourceBuilder.size(5);

            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder = boolQueryBuilder.
                        must(QueryBuilders.rangeQuery("log.ts").format("strict_date_optional_time")
                                    .lte(new Date()));

            sourceBuilder.query(boolQueryBuilder);

            List<Object> result = esService.search(EsClientUtil.getLssEsClient(),
                        "cdn_srs_v3_logs_*", sourceBuilder, Object.class);
            for (int i = 0; i < 5; i++) {
                System.out.println(result.get(i));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/ngdtest")
    @ResponseBody
    public String ngdTest(@RequestBody String body, @RequestHeader("user") String user) {
        log.info("ngd请求body：" + body);
        log.info("ngd请求header user：" + user);
        Map test = new HashMap<>();
        Map data = new HashMap<>();
        Map context = new HashMap();
        test.put("errno", 333);
        test.put("msg", "successful");
        test.put("data", data);
        data.put("context", context);
        data.put("value", "rules");
        context.put("api_response_status", true);
        return JSON.toJSONString(test);
    }
}
