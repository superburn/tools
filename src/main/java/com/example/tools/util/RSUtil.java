package com.example.tools.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class RSUtil {
    public static ResponseEntity rtExchange(String url) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        HttpEntity<String> entity = new HttpEntity(null, null);
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url,
                    HttpMethod.GET, (entity), JSONObject.class);
        return exchange;
    }

    public static ResponseEntity rtExchange2(String url) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        HttpEntity<String> entity = new HttpEntity(null, null);
        ResponseEntity<JSONArray> exchange = restTemplate.exchange(url,
                    HttpMethod.GET, (entity), JSONArray.class);
        return exchange;
    }

    public static <T> ResponseEntity rtExchange3(String url, T body, MultiValueMap<String, String> headers,
                                                 HttpMethod method) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        HttpEntity<String> entity = new HttpEntity(body, headers);
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url,
                    method, (entity), JSONObject.class);
        return exchange;
    }
}
