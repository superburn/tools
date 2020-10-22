package com.example.tools.controller;

import com.alibaba.fastjson.JSON;
import com.example.tools.util.TimeUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PageController {

    @RequestMapping(value = "/fpsChart", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView fpsChart(@RequestParam(value = "stream") String stream,
                                 @RequestParam(value = "endTime") String endTimeStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Date date = null;
        String endTime = null;
        try {
            date = sdf.parse(endTimeStr);
            endTime = sdf1.format(date);
        } catch (ParseException pe) {

        }

        ModelAndView view = new ModelAndView();
        view.addObject("stream", stream);
        view.addObject("endTime", endTime);
        view.setViewName("fpsChart");

        return view;
    }

    @RequestMapping(value = "/details", method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView details(@RequestParam(value = "nodes", required = false) String nodes,
                                @RequestParam(value = "hosts", required = false) String hosts,
                                @RequestParam(value = "endTime") String endTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        Date date = null;
        long startTs = 0L;
        long endTs = 0L;
        try {
            date = sdf.parse(endTime);
            startTs = TimeUtil.addDate(date, -2).getTime() / 1000;
            endTs = TimeUtil.addDate(date, 2).getTime() / 1000;
        } catch (ParseException pe) {

        }

        String share_json = "";
//      生成noah share_json
        if (StringUtils.isNotEmpty(hosts)) {
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Map<String, Object> shareMap = new HashMap();
            Map<String, Object> selectedMap = new HashMap();
            shareMap.put("selected", selectedMap);

            shareMap.put("node_id", "200285482");
            shareMap.put("status", "charts");
            shareMap.put("mode", "namespace");
            shareMap.put("predict_time_day", true);
            shareMap.put("predict_time_week", true);
            shareMap.put("predict_compare_common", true);
            shareMap.put("predict_compare_MAJOR", true);
            shareMap.put("start_time", sdf2.format(TimeUtil.addDate(date, -2)));
            shareMap.put("end_time", sdf2.format(TimeUtil.addDate(date, 2)));
            shareMap.put("nsType", "host");

            selectedMap.put("domain_namespace", new ArrayList());
            selectedMap.put("cluster_namespace", new ArrayList());
            selectedMap.put("service_namespace", new ArrayList());
            selectedMap.put("host_namespace", Splitter.on(",").trimResults().splitToList(hosts));
            selectedMap.put("instance_namespace", new ArrayList());
            selectedMap.put("domain_item", new ArrayList());
            selectedMap.put("cluster_item", new ArrayList());
            selectedMap.put("service_item", new ArrayList());
            selectedMap.put("host_item", Lists.newArrayList("MEM_USED_PERCENT", "MEM_USED"));
            selectedMap.put("host_instance_item", new ArrayList());
            selectedMap.put("instance_instance_item", new ArrayList());

            share_json = JSON.toJSONString(shareMap);
        }

        ModelAndView view = new ModelAndView();
        view.addObject("startTs", startTs);
        view.addObject("endTs", endTs);
        view.addObject("nodes", nodes);
        view.addObject("share_json", share_json);
        view.setViewName("details");

        return view;
    }
}
