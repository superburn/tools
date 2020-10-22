package com.example.tools.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.tools.model.QualityChartModel;
import com.example.tools.service.EsService;
import com.example.tools.task.UpdateNodeLibTask;
import com.example.tools.util.EsClientUtil;
import com.example.tools.util.RSUtil;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/quality")
public class QualityQuaryController {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");

    private static DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private EsService esService;

    @Autowired
    private UpdateNodeLibTask updateNodeLibTask;

    @GetMapping(value = "/publish")
    @ResponseBody
    public String streamPublish(@RequestParam(value = "stream") String stream, @RequestParam(value = "endTime") String endTime) {
        List<QualityChartModel> publishList = new ArrayList();
        List<Object> timeList = new ArrayList();

        String pushUrl = getPushUrl(stream);

        ResponseEntity<JSONArray> response = RSUtil.rtExchange2("http://10.70.8.101:8086/streamsPushE?" +
                    "stream=" + pushUrl + "&endTime=" + endTime);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody().size() > 0) {
            JSONObject data = response.getBody().getJSONObject(0);
            JSONArray fps_data = data.getJSONArray("push_fps");
            if (fps_data != null && !fps_data.isEmpty()) {
                JSONArray point = null;
                for (int i = 0; i < fps_data.size(); i++) {
                    point = fps_data.getJSONArray(i);
                    QualityChartModel model = new QualityChartModel();
                    model.setTime(point.get(0).toString());
                    model.setFps(new BigDecimal(point.get(1).toString()));
                    model.setNode(point.get(2).toString());
                    publishList.add(model);
                    timeList.add(point.get(0));
                }
            }
        }

        Map<String, List<QualityChartModel>> pubGroupByNode = publishList.stream().collect(Collectors.groupingBy(QualityChartModel::getNode));
        Map<String, Object> pubMap = new HashMap();
        pubGroupByNode.forEach((k, list) -> {
            Map<String, List<Object>> data = new HashMap();
            data.put("time", list.stream().map(e -> e.getTime()).collect(Collectors.toList()));
            data.put("node", list.stream().map(e -> e.getNode()).collect(Collectors.toList()));
            data.put("fps", list.stream().map(e -> e.getFps()).collect(Collectors.toList()));
            pubMap.put(k, data);
        });
        Set<String> nodeSet = pubGroupByNode.keySet();

        Map responseData = new HashMap<>();
        responseData.put("pub", JSONArray.toJSON(pubMap));
        responseData.put("time", JSONArray.toJSON(timeList));
        responseData.put("nodes", Joiner.on(",").join(nodeSet));
        return JSONArray.toJSONString(responseData);
    }

    @GetMapping(value = "/forwarder")
    @ResponseBody
    public String streamMediaCentral(@RequestParam(value = "stream") String stream, @RequestParam(value = "endTime") String endTime) {
        List<QualityChartModel> fwdList = new ArrayList();
        List<QualityChartModel> innerList = new ArrayList();
        List<QualityChartModel> outerList = new ArrayList();
        List<Object> fwdTimeList = new ArrayList();
        List<Object> innerTimeList = new ArrayList();
        List<Object> outerTimeList = new ArrayList();

        ResponseEntity<JSONObject> response = RSUtil.rtExchange("http://10.70.8.101:8086/streamsLive?" +
                    "stream=" + stream + "&endTime=" + endTime);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody().size() > 0) {
            JSONObject data = response.getBody();
            JSONArray point = null;

            JSONArray fwd_score_data = data.getJSONArray("forward_score");
            JSONArray fwd_fps_data = data.getJSONArray("forward_fps");
            String fwdNodeIp = null;
            if (fwd_score_data != null && !fwd_score_data.isEmpty() && fwd_fps_data != null && !fwd_fps_data.isEmpty()) {
                for (int i = 0; i < fwd_score_data.size(); i++) {
                    point = fwd_score_data.getJSONArray(i);
                    QualityChartModel model = new QualityChartModel();
                    model.setTime(point.get(0).toString());
                    model.setFps(new BigDecimal(fwd_fps_data.getJSONArray(i).get(1).toString()));
                    fwdNodeIp = point.get(3).toString().split("/")[0];
                    if (StringUtils.isNotEmpty(updateNodeLibTask.getNodeNameByIp(fwdNodeIp))) {
                        model.setNode(updateNodeLibTask.getNodeNameByIp(fwdNodeIp));
                    }
                    model.setHost(getHostName(point.get(2).toString()));
                    fwdList.add(model);
                    fwdTimeList.add(point.get(0));
                }
            }

            JSONArray inner_fps_data = data.getJSONArray("inner_fps");
            if (inner_fps_data != null && !inner_fps_data.isEmpty()) {
                for (int i = 0; i < inner_fps_data.size(); i++) {
                    point = inner_fps_data.getJSONArray(i);
                    QualityChartModel model = new QualityChartModel();
                    model.setTime(point.get(0).toString());
                    model.setFps(new BigDecimal(point.get(1).toString()));
                    model.setHost(point.get(2).toString());
                    innerList.add(model);
                    innerTimeList.add(point.get(0));
                }
            }

            JSONArray outer_fps_data = data.getJSONArray("outer_fps");
            if (outer_fps_data != null && !outer_fps_data.isEmpty()) {
                for (int i = 0; i < outer_fps_data.size(); i++) {
                    point = outer_fps_data.getJSONArray(i);
                    QualityChartModel model = new QualityChartModel();
                    model.setTime(point.get(0).toString());
                    model.setFps(new BigDecimal(point.get(1).toString()));
                    model.setHost(point.get(2).toString());
                    outerList.add(model);
                    outerTimeList.add(point.get(0));
                }
            }
        }

        Map<String, Object> fwdMap = new HashMap();
        Map<String, List<QualityChartModel>> fwdGroupByHost = fwdList.stream().collect(Collectors.groupingBy(QualityChartModel::getHost));
        List<QualityChartModel> nodeNotNullList = fwdList.stream().filter(m -> StringUtils.isNotEmpty(m.getNode())).collect(Collectors.toList());
        Map<String, List<QualityChartModel>> fwdGroupByNode = nodeNotNullList.stream().collect(Collectors.groupingBy(QualityChartModel::getNode));
        fwdGroupByHost.forEach((k, list) -> {
            Map<String, List<Object>> data = new HashMap();
            data.put("time", list.stream().map(e -> e.getTime()).collect(Collectors.toList()));
            data.put("node", list.stream().map(e -> e.getNode()).collect(Collectors.toList()));
            data.put("host", list.stream().map(e -> e.getHost()).collect(Collectors.toList()));
            data.put("fps", list.stream().map(e -> e.getFps()).collect(Collectors.toList()));
            fwdMap.put(k, data);
        });

        Map<String, Object> innerMap = new HashMap();
        Map<String, List<QualityChartModel>> innerGroupByHost = innerList.stream().collect(Collectors.groupingBy(QualityChartModel::getHost));
        innerGroupByHost.forEach((k, list) -> {
            Map<String, List<Object>> data = new HashMap();
            data.put("time", list.stream().map(e -> e.getTime()).collect(Collectors.toList()));
            data.put("host", list.stream().map(e -> e.getHost()).collect(Collectors.toList()));
            data.put("fps", list.stream().map(e -> e.getFps()).collect(Collectors.toList()));
            innerMap.put(k, data);
        });

        Map<String, Object> outerMap = new HashMap();
        Map<String, List<QualityChartModel>> outerGroupByHost = outerList.stream().collect(Collectors.groupingBy(QualityChartModel::getHost));
        outerGroupByHost.forEach((k, list) -> {
            Map<String, List<Object>> data = new HashMap();
            data.put("time", list.stream().map(e -> e.getTime()).collect(Collectors.toList()));
            data.put("host", list.stream().map(e -> e.getHost()).collect(Collectors.toList()));
            data.put("fps", list.stream().map(e -> e.getFps()).collect(Collectors.toList()));
            outerMap.put(k, data);
        });

        Set<String> fwd_hostSet = fwdGroupByHost.keySet();
        Set<String> fwd_nodeSet = fwdGroupByNode.keySet();
        Set<String> inner_hostSet = innerGroupByHost.keySet();
        Set<String> outer_hostSet = outerGroupByHost.keySet();

        Map responseData = new HashMap<>();
        responseData.put("fwd", JSONArray.toJSON(fwdMap));
        responseData.put("inner", JSONArray.toJSON(innerMap));
        responseData.put("outer", JSONArray.toJSON(outerMap));
        responseData.put("fwd_time", JSONArray.toJSON(fwdTimeList));
        responseData.put("inner_time", JSONArray.toJSON(innerTimeList));
        responseData.put("outer_time", JSONArray.toJSON(outerTimeList));


        responseData.put("fwd_hosts", Joiner.on(",").join(fwd_hostSet));
        responseData.put("fwd_nodes", Joiner.on(",").join(fwd_nodeSet));
        responseData.put("inner_hosts", Joiner.on(",").join(inner_hostSet));
        responseData.put("outer_hosts", Joiner.on(",").join(outer_hostSet));
        return JSONArray.toJSONString(responseData);
    }

    @GetMapping(value = "/ingester")
    @ResponseBody
    public String streamIngester(@RequestParam(name = "stream", required = false) String stream, @RequestParam(value = "endTime") String endTime) {
        Map<String, Object> dataMap = new HashMap();
        List<QualityChartModel> pointList = new ArrayList();
        Set<String> timeList = new TreeSet();

        LocalDateTime end = LocalDateTime.parse(endTime, formatter2);
        LocalDateTime start = end.minusMinutes(30);

        Date endDate = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
        Date startDate = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());

        try {
            //查询条件
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            sourceBuilder.from(0).size(500);
            RangeQueryBuilder rq = QueryBuilders.rangeQuery("log.ts").format("strict_date_optional_time").gte(startDate).lte(endDate);
            MatchPhraseQueryBuilder mpq1 = QueryBuilders.matchPhraseQuery("session", stream);
            MatchPhraseQueryBuilder mpq2 = QueryBuilders.matchPhraseQuery("log.line", "ingester stats");

            QueryBuilder qb = QueryBuilders.boolQuery().must(rq).must(mpq1).must(mpq2);
            sourceBuilder.query(qb).sort(SortBuilders.fieldSort("log.ts").unmappedType("boolean").order(SortOrder.ASC));
            List<Object> result = esService.search(EsClientUtil.getLssEsClient(),
                        "cdn_srs_v3_logs_*", sourceBuilder, Object.class);

            JSONObject point = null;
            JSONObject log = null;
            String formatDate = null;
            for (int i = 0; i < result.size(); i++) {
                point = (JSONObject) result.get(i);
                log = (JSONObject) point.get("log");
                formatDate = LocalDateTime.parse(log.get("ts").toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                            .format(formatter);
                timeList.add(formatDate);

                QualityChartModel model = new QualityChartModel();
                model.setTime(formatDate);
                model.setNode(log.get("node").toString());
                model.setFps(new BigDecimal(point.get("video_fps").toString()));
                pointList.add(model);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //按节点分组并以时间排序
        Map<String, List<QualityChartModel>> groupByNode = pointList.stream().collect(Collectors.groupingBy(QualityChartModel::getNode));
        groupByNode.forEach((k, list) -> {
            //去重
            List<QualityChartModel> newList = list.stream().collect(Collectors
                        .collectingAndThen(Collectors.toCollection(() ->
                                    new TreeSet<>(Comparator.comparing(QualityChartModel::getTime))), ArrayList::new));
            //补全时间点
            List<String> times = newList.stream().map(o -> o.getTime()).collect(Collectors.toList());
            List<String> diff = timeList.stream().filter(t -> !times.contains(t)).collect(Collectors.toList());
            diff.forEach(d -> {
                QualityChartModel model = new QualityChartModel();
                model.setTime(d);
                model.setNode(k);
                newList.add(model);
            });
            //排序
            List<QualityChartModel> newList2 = newList.stream().sorted(Comparator.comparing(QualityChartModel::getTime)).collect(Collectors.toList());
            //填充fps空值,当fps为null时,取上一个时间点fps值
            BigDecimal fps = null;
            QualityChartModel model = null;
            for (int i = 0; i < newList2.size(); i++) {
                model = newList2.get(i);
                model.setFps(i > 0 && model.getFps() == null ? newList2.get(i - 1).getFps() : model.getFps());
            }

            Map<String, List<Object>> data = new HashMap();
            data.put("time", newList2.stream().map(e -> e.getTime()).collect(Collectors.toList()));
            data.put("node", newList2.stream().map(e -> e.getNode()).collect(Collectors.toList()));
            data.put("fps", newList2.stream().map(e -> e.getFps()).collect(Collectors.toList()));
            dataMap.put(k, data);
        });

        Set<String> nodeSet = dataMap.keySet();

        Map<String, Object> responseMap = new HashMap();
        responseMap.put("node", dataMap);
        responseMap.put("timeList", timeList);
        responseMap.put("nodes", Joiner.on(",").join(nodeSet));

        return JSONArray.toJSONString(responseMap);
    }

    //todo 代码重复
    @GetMapping(value = "/play")
    @ResponseBody
    public String streamPlay(@RequestParam(name = "stream") String stream, @RequestParam(value = "endTime") String endTime) {
        Map<String, Object> dataMap = new HashMap();
        List<QualityChartModel> pointList = new ArrayList();
        Set<String> timeList = new TreeSet();

        LocalDateTime end = LocalDateTime.parse(endTime, formatter2);
        LocalDateTime start = end.minusMinutes(30);

        Date endDate = Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
        Date startDate = Date.from(start.atZone(ZoneId.systemDefault()).toInstant());

        try {
            //查询条件
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

            sourceBuilder.from(0).size(500);
            RangeQueryBuilder rq = QueryBuilders.rangeQuery("log.ts").format("strict_date_optional_time").gte(startDate).lte(endDate);
            MatchPhraseQueryBuilder mpq1 = QueryBuilders.matchPhraseQuery("session", stream);
            MatchPhraseQueryBuilder mpq2 = QueryBuilders.matchPhraseQuery("log.line", "play stats");

            QueryBuilder qb = QueryBuilders.boolQuery().must(rq).must(mpq1).must(mpq2);
            sourceBuilder.query(qb).sort(SortBuilders.fieldSort("log.ts").unmappedType("boolean").order(SortOrder.ASC));

            List<Object> result = esService.search(EsClientUtil.getLssEsClient(),
                        "cdn_srs_v3_logs_*", sourceBuilder, Object.class);

            JSONObject point = null;
            JSONObject log = null;
            String formatDate = null;
            for (int i = 0; i < result.size(); i++) {
                point = (JSONObject) result.get(i);
                log = (JSONObject) point.get("log");
                formatDate = LocalDateTime.parse(log.get("ts").toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                            .format(formatter);
                timeList.add(formatDate);

                QualityChartModel model = new QualityChartModel();
                model.setTime(formatDate);
                model.setNode(log.get("node").toString());
                model.setFps(new BigDecimal(point.get("video_fps").toString()));
                pointList.add(model);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //按节点分组并以时间排序
        Map<String, List<QualityChartModel>> groupByNode = pointList.stream().collect(Collectors.groupingBy(QualityChartModel::getNode));
        groupByNode.forEach((k, list) -> {
            //去重
            List<QualityChartModel> newList = list.stream().collect(Collectors
                        .collectingAndThen(Collectors.toCollection(() ->
                                    new TreeSet<>(Comparator.comparing(QualityChartModel::getTime))), ArrayList::new));
            //补全时间点
            List<String> times = newList.stream().map(o -> o.getTime()).collect(Collectors.toList());
            List<String> diff = timeList.stream().filter(t -> !times.contains(t)).collect(Collectors.toList());
            diff.forEach(d -> {
                QualityChartModel model = new QualityChartModel();
                model.setTime(d);
                model.setNode(k);
                newList.add(model);
            });
            //排序
            List<QualityChartModel> newList2 = newList.stream().sorted(Comparator.comparing(QualityChartModel::getTime)).collect(Collectors.toList());
            //填充fps空值,当fps为null时,取上一个时间点fps值
            BigDecimal fps = null;
            QualityChartModel model = null;
            for (int i = 0; i < newList2.size(); i++) {
                model = newList2.get(i);
                model.setFps(i > 0 && model.getFps() == null ? newList2.get(i - 1).getFps() : model.getFps());
            }

            Map<String, List<Object>> data = new HashMap();
            data.put("time", newList2.stream().map(e -> e.getTime()).collect(Collectors.toList()));
            data.put("node", newList2.stream().map(e -> e.getNode()).collect(Collectors.toList()));
            data.put("fps", newList2.stream().map(e -> e.getFps()).collect(Collectors.toList()));
            dataMap.put(k, data);
        });

        Set<String> nodeSet = dataMap.keySet();

        Map<String, Object> responseMap = new HashMap();
        responseMap.put("node", dataMap);
        responseMap.put("timeList", timeList);
        responseMap.put("nodes", Joiner.on(",").join(nodeSet));

        return JSONArray.toJSONString(responseMap);
    }

    public String getPushUrl(String stream) {
        String[] das = stream.split("/");
        ResponseEntity<JSONObject> response = RSUtil.rtExchange("http://gzns.master.bcelive.com/internal/v3/ts/query/domain/" + das[0]);
        JSONObject data = response.getBody();
        String pushDomain = data.getString("push");
        if (StringUtils.isEmpty(pushDomain)) {
            return null;
        }
        return Joiner.on("/").join(pushDomain, das[1], das[2]);
    }

    private String getHostName(String ip) {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            return null;
        }
        return inetAddress.getHostName().replaceAll(".baidu.com", "");
    }
}
