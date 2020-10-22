package com.example.tools.dao;


import org.apache.ibatis.annotations.Param;

public interface OneMinuteDomainStatisticsMapper {
    Long getDomainDownStreamByUserIdAndTime(@Param("userId") String userId,
                                            @Param("domain") String domain,
                                            @Param("startTime") String startTime,
                                            @Param("endTime") String endTime);
}
