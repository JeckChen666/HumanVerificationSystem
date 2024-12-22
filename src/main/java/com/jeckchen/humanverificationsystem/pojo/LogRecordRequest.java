package com.jeckchen.humanverificationsystem.pojo;

import lombok.Data;

@Data
public class LogRecordRequest {
    private String sessionId;
    private String ip;
    private String url;
    private String country;
    private String city;
    private String deviceInfo;
    private String operation;
    private String firstVisit;
    private String startDate;
    private String endDate;
    private Integer page;
    private Integer size;
}
