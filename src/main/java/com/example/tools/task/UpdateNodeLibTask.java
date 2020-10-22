package com.example.tools.task;

import com.alibaba.fastjson.JSONArray;
import com.example.tools.util.LoadPropertyUtil;
import com.example.tools.util.RSUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class UpdateNodeLibTask implements Runnable {

    private static Map<String, Object> nodeLib = new ConcurrentHashMap();

    @Override
    public void run() {
        initDataMap();
    }

    public void initDataMap() {
        ResponseEntity<JSONArray> response = RSUtil.rtExchange2((String) LoadPropertyUtil.getProperty("cdnNode.lib"));

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            JSONArray body = response.getBody();
            if (body.size() > 0) {
                String ipRange = "";
                String[] ipSplit = null;

                for (int i = 0; i < body.size(); i++) {
                    ipRange = ((LinkedHashMap<String, Object>) body.get(i)).get("iprange").toString();
                    ipSplit = ipRange.split("\\.");

                    Object first = null;
                    Object second = null;
                    Object third = null;
                    first = nodeLib.get(ipSplit[0]);
                    if (first == null) {
                        first = new HashMap();
                        nodeLib.put(ipSplit[0], first);
                    } else {
                        second = ((HashMap) first).get(ipSplit[1]);
                    }

                    if (second == null) {
                        second = new HashMap();
                        ((HashMap) first).put(ipSplit[1], second);
                    } else {
                        third = ((HashMap) second).get(ipSplit[2]);
                    }

                    if (third == null) {
                        third = new HashMap();
                        ((HashMap) second).put(ipSplit[2], third);
                        ((HashMap) third).put("name", ((LinkedHashMap<String, Object>) body.get(i)).get("name"));
                    }


                }
            }
            nodeLib.keySet();
        }
    }

    public String getNodeNameByIp(String ip) {
        String[] ipSplit = ip.split("\\.");
        if (nodeLib.get(ipSplit[0]) == null) {
            return null;
        }
        if (((HashMap) nodeLib.get(ipSplit[0])).get(ipSplit[1]) == null) {
            return null;
        }
        if (((HashMap) ((HashMap) nodeLib.get(ipSplit[0])).get(ipSplit[1])).get(ipSplit[2]) == null) {
            return null;
        }
        return ((HashMap) ((HashMap) ((HashMap)
                    nodeLib.get(ipSplit[0])).get(ipSplit[1])).get(ipSplit[2]))
                    .get("name").toString();
    }
}
