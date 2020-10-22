package com.example.tools.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.tools.model.RobotMessageModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class HiRobotUtil {
    public static void pushMessage(RobotMessageModel robotMessageModel) {
        Long groupId = robotMessageModel.getToId();
        String accessToken = robotMessageModel.getAccessToken();
        String content = robotMessageModel.getContent();
        String fromUser = robotMessageModel.getFromUser();
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        String url = "http://apiin.im.baidu.com/api/msg/groupmsgsend?access_token=" + accessToken;

        JSONArray toid = new JSONArray();
        toid.add(groupId);
        JSONObject header = new JSONObject();
        header.put("toid", toid);

        JSONArray body = null;
        try {
            body = generateBody(body, content);
        } catch (Exception e) {
            body = new JSONArray();
            JSONObject bodyContent = new JSONObject();
            bodyContent.put("type", "TEXT");
            bodyContent.put("content", content);
            body.add(bodyContent);
        }

        if (StringUtils.isNotEmpty(fromUser)) {
            JSONObject atJson = new JSONObject();
            JSONArray atUserIds = new JSONArray();
            atUserIds.add(fromUser);
            atJson.put("type", "AT");
            atJson.put("atall", false);
            atJson.put("atuserids", atUserIds);
            body.add(atJson);
        }

        JSONObject message = new JSONObject();
        message.put("header", header);
        message.put("body", body);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", message);

        HttpEntity<String> entity = new HttpEntity<>(jsonObject.toString(), null);
        ResponseEntity<JSONObject> exchange = restTemplate.exchange(url,
                    HttpMethod.POST, (entity), JSONObject.class);
        log.info("HiRobotUtil push message response,{}", exchange.toString());
    }

    public static JSONArray generateBody(JSONArray body, String str) {
        if (body == null) {
            body = new JSONArray();
        }
        String urlPattern = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)" +
                    "+([\\w\\-\\.,@?^=%&:/~\\+#]*[\\w\\-\\@?^=%&/~\\+#])?";
        String domainPattern = "[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})" +
                    "+\\.(?:com|net|org|edu|gov|biz|tv|me|pro|name|cc|co|info|cm)";
        String ipPattern = "((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}";
        Pattern urlP = Pattern.compile(urlPattern);
        Pattern domainP = Pattern.compile(domainPattern);
        Pattern ipP = Pattern.compile(ipPattern);
        Matcher m = null;
        Matcher m1 = null;
        Matcher m2 = null;
        Matcher m3 = null;

        int end = str.length();
        for (int start = 0; str.length() > 0; ) {
            m1 = urlP.matcher(str);
            m2 = domainP.matcher(str);
            m3 = ipP.matcher(str);
            m = m1.find() ? m1 : m2.find() ? m2 : m3.find() ? m3 : null;
            if (m != null) {
                if (m.start(0) > 0) {
                    JSONObject content = new JSONObject();
                    content.put("type", "TEXT");
                    content.put("content", str.substring(0, m.start(0)));
                    body.add(content);
                }

                JSONObject url = new JSONObject();
                url.put("type", "LINK");
                url.put("href", m.group(0));
                body.add(url);
                start = m.end(0);
                if (start < end) {
                    str = str.substring(start);
                }
            } else {
                JSONObject content = new JSONObject();
                content.put("type", "TEXT");
                content.put("content", str);
                body.add(content);
                break;
            }
        }
        return body;
    }
}









