package com.example.tools.model;

import java.util.Date;

public class OneMinuteDomainStatistics {
    private Date time;

    private String user;

    private String domain;

    private Long downstreamRtmp;

    private Long requestCountRtmp;

    private Long downstreamFlv;

    private Long requestCountFlv;

    private Long downstreamHls;

    private Long requestCountHls;

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user == null ? null : user.trim();
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain == null ? null : domain.trim();
    }

    public Long getDownstreamRtmp() {
        return downstreamRtmp;
    }

    public void setDownstreamRtmp(Long downstreamRtmp) {
        this.downstreamRtmp = downstreamRtmp;
    }

    public Long getRequestCountRtmp() {
        return requestCountRtmp;
    }

    public void setRequestCountRtmp(Long requestCountRtmp) {
        this.requestCountRtmp = requestCountRtmp;
    }

    public Long getDownstreamFlv() {
        return downstreamFlv;
    }

    public void setDownstreamFlv(Long downstreamFlv) {
        this.downstreamFlv = downstreamFlv;
    }

    public Long getRequestCountFlv() {
        return requestCountFlv;
    }

    public void setRequestCountFlv(Long requestCountFlv) {
        this.requestCountFlv = requestCountFlv;
    }

    public Long getDownstreamHls() {
        return downstreamHls;
    }

    public void setDownstreamHls(Long downstreamHls) {
        this.downstreamHls = downstreamHls;
    }

    public Long getRequestCountHls() {
        return requestCountHls;
    }

    public void setRequestCountHls(Long requestCountHls) {
        this.requestCountHls = requestCountHls;
    }
}
