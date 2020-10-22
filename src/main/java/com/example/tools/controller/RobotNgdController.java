package com.example.tools.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.tools.model.AccountMessageModel;
import com.example.tools.model.RobotMessageModel;
import com.example.tools.service.NgdService;
import com.example.tools.util.HiRobotUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/kg")
@Slf4j
public class RobotNgdController {
    @Autowired
    private NgdService ngdService;

    /**
     * robot 入口
     *
     * @param httpServletRequest
     * @param httpServletResponse
     */
    @RequestMapping(value = "/lssRobot")
    public void operateLssKgFromRobot(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            String echostr = httpServletRequest.getParameter("echostr");
            if (echostr != null && echostr.length() != 0) {
                httpServletResponse.getWriter().print(echostr);
            }
        } catch (Exception e) {
            log.error("echostr error", e);
        }

        RobotMessageModel messageModel = decrypt(httpServletRequest);
        if (messageModel.getCommandName() != null && messageModel.getCommandName().equals("a")) {
            ngdService.addKg(messageModel);
        } else {
            messageModel.setSessionId(messageModel.getFromUser() + messageModel.getToId());
            String answer = ngdService.coreQuery(messageModel);
            RobotMessageModel robotMessageModel = new RobotMessageModel();
            robotMessageModel.setToId(messageModel.getToId());
            robotMessageModel.setAccessToken("dcd99015a981939928f2188f333f68674");
            robotMessageModel.setContent(answer.toString());
            robotMessageModel.setFromUser(messageModel.getFromUser());
            HiRobotUtil.pushMessage(robotMessageModel);
        }
    }

    /**
     * robot消息解析
     *
     * @param httpServletRequest
     * @return
     */
    private RobotMessageModel decrypt(HttpServletRequest httpServletRequest) {
        RobotMessageModel messageModel = new RobotMessageModel();

        try {
            byte[] msgBase64 = Base64.decodeBase64(IOUtils.toString(httpServletRequest.getInputStream(), "utf-8"));
            Charset charset = Charset.forName("utf-8");
            SecretKeySpec skeySpec = new SecretKeySpec(Base64.decodeBase64("PkZz5U9oP66Cvl8BIbLSkY"), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] decrypted = cipher.doFinal(msgBase64);
            // 通过AES解密后得到回调消息数据
            String msgJsonStr = new String(decrypted, charset);
            log.info("接收robot message:{}", msgJsonStr);
            JSONObject msgJson = JSON.parseObject(msgJsonStr);
            JSONObject message = (JSONObject) msgJson.get("message");
            JSONObject header = (JSONObject) message.get("header");
            messageModel.setToId(header.get("toid") instanceof Long ?
                        (Long) header.get("toid") : ((Integer) header.get("toid")).longValue());
            messageModel.setFromUser((String) header.get("fromuserid"));
            JSONArray body = (JSONArray) message.get("body");
            JSONObject bodyJsonObject = null;
            StringBuilder textMsg = new StringBuilder();
            for (int i = 0; i < body.size(); i++) {
                bodyJsonObject = (JSONObject) body.get(i);
                if (bodyJsonObject.get("type").equals("command")) {
                    messageModel.setCommandName((String) bodyJsonObject.get("commandname"));
                } else if (bodyJsonObject.get("type").equals("TEXT")) {
                    textMsg.append((String) bodyJsonObject.get("content"));
                } else if (bodyJsonObject.get("type").equals("LINK")) {
                    textMsg.append((String) bodyJsonObject.get("label"));
                }
            }
            messageModel.setContent((String) textMsg.toString().trim());
            log.info("接收robot message, text content:{}", JSON.toJSONString(messageModel));
        } catch (Exception e) {
            log.error("decrypt error", e);
        }
        return messageModel;
    }

    @RequestMapping(value = "/lssAccount")
    @ResponseBody
    public String operateLssKgFromAccount(@RequestParam(value = "content") String content,
                                          @RequestParam(value = "fromUser") String fromUser) {

        AccountMessageModel messageModel = new AccountMessageModel();
        messageModel.setContent(content);
        messageModel.setFromUser(fromUser);
        messageModel.setSessionId(fromUser);
        Map result = new HashMap();
        result.put("result", ngdService.coreQuery(messageModel));
        return JSON.toJSONString(result);
    }
}
