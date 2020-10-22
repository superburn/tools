package com.example.tools.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.tools.model.MessageModel;
import com.example.tools.model.RobotMessageModel;
import com.example.tools.util.HiRobotUtil;
import com.example.tools.util.RSUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class NgdService {

    /**
     * 会话中控
     *
     * @param messageModel
     */
    public String coreQuery(MessageModel messageModel) {
        StringBuilder answer = new StringBuilder();

        Map<String, Object> body = new HashMap<>();
        body.put("queryText", messageModel.getContent());
        body.put("sessionId", messageModel.getSessionId());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "NGD e6d8b844-222d-4891-b90e-ee1346005f17");
        headers.add("Content-Type", "application/json;charset=UTF-8");

        ResponseEntity<JSONObject> response = RSUtil.rtExchange3("https://api-ngd.baidu.com/api/v2/core/query",
                    JSON.toJSONString(body), headers, HttpMethod.POST);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody().size() > 0) {
            if (response.getBody().get("code").equals(200)) {
                List<Object> faqList = new ArrayList();

                //faq列表，查询相似推荐
                HttpHeaders faqSearchHeader = new HttpHeaders();
                faqSearchHeader.add("Authorization", "NGD dec81ccd-2d4b-4e3a-8191-8f9bfaf1d9cc");
                faqSearchHeader.add("Content-Type", "application/json;charset=UTF-8");

                ResponseEntity<JSONObject> faqSearchResponse = RSUtil.rtExchange3("https://openapi-ngd.baidu.com/open/v2/faq/standard?" +
                                        "searchType=standard&keyword=" + messageModel.getContent(),
                            null, faqSearchHeader, HttpMethod.GET);
                if (faqSearchResponse.getStatusCode().is2xxSuccessful() && faqSearchResponse.getBody().size() > 0) {
                    if (faqSearchResponse.getBody().get("code").equals(200)) {
                        Map faqSearchData = (HashMap) faqSearchResponse.getBody().get("data");
                        faqList = (List<Object>) faqSearchData.get("list");
                    }
                }

                //建议答案
                Map data = (HashMap) response.getBody().get("data");
                String suggestAnswer = data.get("suggestAnswer") == null ? null : data.get("suggestAnswer").toString();
                String faqId = data.get("answer") == null ? null : ((Map) data.get("answer")).get("faq") == null ?
                            null : (String) ((Map) ((Map) data.get("answer")).get("faq")).get("id");

                if (faqList != null && faqList.size() > 0) {
                    StringBuilder recommend = new StringBuilder();
                    faqList.stream().forEach(faq -> {
                        String question = (String) ((Map) faq).get("question");
                        String id = (String) ((Map) faq).get("id");
                        if (!id.equals(faqId)) {
                            recommend.append(question).append("\n");
                        }
                    });

                    if (suggestAnswer != null && !suggestAnswer.equals("抱歉,我不太理解您的意思")) {
                        answer.append(suggestAnswer);
                        if (recommend.length() > 0) {
                            answer.append("\n").append("\n").append("相似推荐：").append("\n").append(recommend);
                        }
                    } else {
                        answer.append("请问你想询问的是？").append("\n").append("\n").append(recommend);
                    }
                } else {
                    if (suggestAnswer != null && !suggestAnswer.equals("抱歉,我不太理解您的意思")) {
                        answer.append(suggestAnswer);
                    } else {
                        answer.append("抱歉,我不太理解您的意思");
                    }
                }
            }
        }
        return answer.toString();
    }

    /**
     * 添加问答库
     *
     * @param messageModel
     */
    public void addKg(RobotMessageModel messageModel) {
        String qRegex = "\\[{1}(.+)]";
        Pattern p = Pattern.compile(qRegex);
        Matcher m = p.matcher(messageModel.getContent());
        if (m.find()) {
            String question = m.group(0);
            String content = messageModel.getContent().substring(messageModel.getContent().indexOf(question) + question.length() + 1);
            Map<String, Object> body = new HashMap<>();
            Map<String, Object> answer = new HashMap<>();
            body.put("question", question);
            body.put("answer", answer);
            answer.put("text", content);
            answer.put("type", 1);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "NGD dec81ccd-2d4b-4e3a-8191-8f9bfaf1d9cc");
            headers.add("Content-Type", "application/json;charset=UTF-8");

            ResponseEntity<JSONObject> response = RSUtil.rtExchange3("https://openapi-ngd.baidu.com/open/v2/faq/standard/create",
                        JSON.toJSONString(body), headers, HttpMethod.POST);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody().size() > 0) {
                if (response.getBody().get("code").equals(200)) {
                    RobotMessageModel robotMessageModel = new RobotMessageModel();
                    robotMessageModel.setToId(messageModel.getToId());
                    robotMessageModel.setAccessToken("dcd99015a981939928f2188f333f68674");
                    robotMessageModel.setContent("添加" + question + "成功，正在发布...");
                    HiRobotUtil.pushMessage(robotMessageModel);

                    Map<String, Object> publishBody = new HashMap<>();
                    publishBody.put("category", "faq");

                    HttpHeaders publishHeaders = new HttpHeaders();
                    publishHeaders.add("Authorization", "NGD dec81ccd-2d4b-4e3a-8191-8f9bfaf1d9cc");
                    publishHeaders.add("Content-Type", "application/json;charset=UTF-8");

                    ResponseEntity<JSONObject> publishResponse = RSUtil.rtExchange3("https://openapi-ngd.baidu.com/open/v1/publish/knowledge",
                                JSON.toJSONString(publishBody), publishHeaders, HttpMethod.POST);
                    if (publishResponse.getStatusCode().is2xxSuccessful() && publishResponse.getBody().size() > 0) {
                        if (publishResponse.getBody().get("code").equals(200)) {
                            RobotMessageModel robotMessageModel2 = new RobotMessageModel();
                            robotMessageModel2.setToId(messageModel.getToId());
                            robotMessageModel2.setAccessToken("dcd99015a981939928f2188f333f68674");
                            robotMessageModel2.setContent("发布" + question + "成功");
                            HiRobotUtil.pushMessage(robotMessageModel2);
                        }
                    }
                }
            }
        }
    }
}
