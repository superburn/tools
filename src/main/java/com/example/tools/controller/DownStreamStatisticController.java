package com.example.tools.controller;

import com.example.tools.service.DownStreamStatisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/downStreamSta")
public class DownStreamStatisticController {
    @Autowired
    private DownStreamStatisticService downStreamStatisticService;

    /**
     * 查询域名级别流量
     * @param userId
     * @param domain
     * @param startTime
     * @param endTime
     * @return
     */
    @RequestMapping(value = "/domain")
    @ResponseBody
    public String downStreamStaDomain(@RequestParam(value = "userId") String userId,
                                      @RequestParam(value = "domain") String domain,
                                      @RequestParam(value = "startTime") String startTime,
                                      @RequestParam(value = "endTime") String endTime) {
        Long sumDownstream = downStreamStatisticService
                    .getDomainDownStreamByUserIdAndTime(userId, domain, startTime, endTime);
        return sumDownstream.toString();
    }


}
