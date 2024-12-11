package com.jeckchen.humanverificationsystem.service;

import com.jeckchen.humanverificationsystem.pojo.LogRecord;
import com.jeckchen.humanverificationsystem.repository.LogRecordRepository;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class LogRecordService {

    @Resource
    private LogRecordRepository logRecordRepository;

    public Page<LogRecord> findAllWithFilters(
            String sessionId, String ip, String url, String country, String city, String deviceInfo,
            String operation, String firstVisit, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {

        // 过滤掉空字符串
        sessionId = sessionId != null && !sessionId.trim().isEmpty() ? sessionId.trim() : null;
        ip = ip != null && !ip.trim().isEmpty() ? ip.trim() : null;
        url = url != null && !url.trim().isEmpty() ? url.trim() : null;
        country = country != null && !country.trim().isEmpty() ? country.trim() : null;
        city = city != null && !city.trim().isEmpty() ? city.trim() : null;
        deviceInfo = deviceInfo != null && !deviceInfo.trim().isEmpty() ? deviceInfo.trim() : null;
        operation = operation != null && !operation.trim().isEmpty() ? operation.trim() : null;
        firstVisit = firstVisit != null && !firstVisit.trim().isEmpty() ? firstVisit.trim() : null;

        return logRecordRepository.findAllWithFilters(
                sessionId, ip, url, country, city, deviceInfo, operation, firstVisit, startDate, endDate, pageable);
    }

    public List<LogRecord> findAllWithFilters(
            String sessionId, String ip, String url, String country, String city, String deviceInfo,
            String operation, String firstVisit, LocalDateTime startDate, LocalDateTime endDate) {

        // 过滤掉空字符串
        sessionId = sessionId != null && !sessionId.trim().isEmpty() ? sessionId.trim() : null;
        ip = ip != null && !ip.trim().isEmpty() ? ip.trim() : null;
        url = url != null && !url.trim().isEmpty() ? url.trim() : null;
        country = country != null && !country.trim().isEmpty() ? country.trim() : null;
        city = city != null && !city.trim().isEmpty() ? city.trim() : null;
        deviceInfo = deviceInfo != null && !deviceInfo.trim().isEmpty() ? deviceInfo.trim() : null;
        operation = operation != null && !operation.trim().isEmpty() ? operation.trim() : null;
        firstVisit = firstVisit != null && !firstVisit.trim().isEmpty() ? firstVisit.trim() : null;

        return logRecordRepository.findAllWithFilters(
                sessionId, ip, url, country, city, deviceInfo, operation, firstVisit, startDate, endDate);
    }
}