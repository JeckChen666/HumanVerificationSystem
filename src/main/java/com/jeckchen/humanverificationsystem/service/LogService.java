package com.jeckchen.humanverificationsystem.service;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.jeckchen.humanverificationsystem.pojo.IpApiResp;
import com.jeckchen.humanverificationsystem.pojo.LogRecord;
import com.jeckchen.humanverificationsystem.pojo.VerificationRequest;
import com.jeckchen.humanverificationsystem.repository.LogRecordRepository;
import com.jeckchen.humanverificationsystem.utils.IpUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class LogService {

    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    Cache<String, IpApiResp> lruCache = CacheUtil.newLRUCache(20);

    TimedCache<String, String> timedCache = CacheUtil.newTimedCache(DateUnit.HOUR.getMillis() * 6, DateUnit.MINUTE.getMillis() * 5);

    @Resource
    private IpApiRestTemplate ipApiRestTemplate;

    @Resource
    private LogRecordRepository logRecordRepository;

    private static final String JSON_DIR = "json/";

    public void logAccess(HttpServletRequest request) {
        String operation = "访问主页";
        LogRecord record = constructLogRecord(request, operation);
        saveLogRecord(record);
    }

    public void logAccess(HttpServletRequest request, String pageName) {
        String operation = "访问页面：" + pageName;
        LogRecord record = constructLogRecord(request, operation);
        saveLogRecord(record);
    }

    @Async
    public void logVerification(HttpServletRequest request, VerificationRequest verificationRequest, boolean isCorrect) {
        String operation = "验证题目：" + verificationRequest.getQuestion() + " 给出的答案：" + verificationRequest.getAnswer() + " 是否正确：" + isCorrect;
        LogRecord record = constructLogRecord(request, operation);
        saveLogRecord(record);
    }

    private LogRecord constructLogRecord(HttpServletRequest request, String operation) {
        String time = DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
        String sessionId = request.getSession()
                                  .getId();
        String ip = IpUtil.getIpAddr(request);
        String requestURI = request.getRequestURI();
        IpApiResp ipApiResp = useApi(ip);
        String region = ipApiResp.getCountry_name();
        String city = ipApiResp.getCity();
        String deviceInfo = request.getHeader("User-Agent");
        String firstVisit = getFirstVisit(sessionId);
        return LogRecord.builder()
                        .time(time)
                        .sessionId(sessionId)
                        .ip(ip)
                        .url(requestURI)
                        .country(region)
                        .city(city)
                        .deviceInfo(deviceInfo)
                        .operation(operation)
                        .firstVisit(firstVisit)
                        .build();
    }


    private void saveLogRecord(LogRecord entity) {
//        CompletableFuture.runAsync(() -> {
//            saveToJson(entity);
//        }, executor);
        CompletableFuture.runAsync(() -> {
            saveToDB(entity);
        }, executor);
    }

    private void saveToJson(LogRecord entity) {
        String fileName = JSON_DIR + "sysLog" + DateUtil.format(LocalDateTime.now(), "yyyyMMdd") + ".txt";
        ensureDirectoryExists(JSON_DIR);
        String jsonStr = JSONUtil.toJsonStr(entity);
        try (FileWriter fileWriter = new FileWriter(fileName, true)) {
            fileWriter.write(jsonStr + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToDB(LogRecord entity) {
        logRecordRepository.save(entity);
    }

    private void ensureDirectoryExists(String dir) {
        File directory = new File(dir);
        if (!directory.exists()) {
            synchronized (this) { // 使用同步块确保线程安全
                if (!directory.exists()) {
                    try {
                        boolean created = directory.mkdirs();
                        if (!created) {
                            throw new IOException("Failed to create directory: " + dir);
                        }
                    } catch (SecurityException e) {
                        throw new RuntimeException("Permission denied: " + dir, e);
                    } catch (IOException e) {
                        throw new RuntimeException("Error creating directory: " + dir, e);
                    }
                }
            }
        }
    }

    private String getFirstVisit(String sessionId) {
        String timeResp;
        timeResp = timedCache.get(sessionId);
        if (StringUtils.isBlank(timeResp)) {
            String time = DateUtil.format(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss");
            timedCache.put(sessionId, time);
            return time;
        }
        return timeResp;
    }

    private IpApiResp useApi(String ip) {
        IpApiResp locationOfIp;
        locationOfIp = lruCache.get(ip);
        if (null != locationOfIp) {
            return locationOfIp;
        }
        try {
            locationOfIp = ipApiRestTemplate.getLocationOfIp(ip);
            log.info("use \"ipapi.co\" api Success, response:{}", JSONUtil.toJsonStr(locationOfIp));
        } catch (RuntimeException e) {
            log.error("use \"ipapi.co\" api Error", e);
        }
        if (null != locationOfIp) {
            lruCache.put(ip, locationOfIp, DateUnit.HOUR.getMillis() * 12);
            return locationOfIp;
        }
        return IpApiResp.getUnknown();
    }
}