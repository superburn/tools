package com.example.tools.mapperutil;


import com.example.tools.dao.OneMinuteDomainStatisticsMapper;
import com.example.tools.util.SpringUtils;

public class StaMapperUtil {
    private static OneMinuteDomainStatisticsMapper oneMinuteDomainStatisticsMapper
            = (OneMinuteDomainStatisticsMapper) SpringUtils.getBean(OneMinuteDomainStatisticsMapper.class);

    public static Long getDomainDownStreamByUserIdAndTime
                (String userId, String domain, String startTime, String endTime) {
        return oneMinuteDomainStatisticsMapper.getDomainDownStreamByUserIdAndTime(userId, domain, startTime, endTime);
    }
}
