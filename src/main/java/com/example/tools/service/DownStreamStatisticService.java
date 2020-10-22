package com.example.tools.service;

import com.example.tools.mapperutil.StaMapperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DownStreamStatisticService {
    public Long getDomainDownStreamByUserIdAndTime(String userId, String domain,
                                                                    String startTime, String endTime) {
        return StaMapperUtil.getDomainDownStreamByUserIdAndTime(userId, domain, startTime, endTime);
    }
}
