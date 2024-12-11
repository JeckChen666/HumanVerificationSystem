package com.jeckchen.humanverificationsystem.controller;

import cn.hutool.core.date.DateUtil;
import com.jeckchen.humanverificationsystem.config.RateLimit;
import com.jeckchen.humanverificationsystem.pojo.LogRecord;
import com.jeckchen.humanverificationsystem.service.LogRecordService;
import com.jeckchen.humanverificationsystem.service.VerificationService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
public class LogRecordController {

    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Resource
    private VerificationService verificationService;

    @Resource
    private LogRecordService logRecordService;

    @RateLimit(ipLimit = 5, globalLimit = 100)
    @GetMapping("/log")
    public String showLogRecordsPage(Model model, HttpServletRequest request) {
        CompletableFuture.runAsync(() -> verificationService.logAccess(request, "/log"), executor);
        // 这里可以传递一些默认值或空值到前端
        model.addAttribute("sessionId", "");
        model.addAttribute("ip", "");
        model.addAttribute("url", "");
        model.addAttribute("country", "");
        model.addAttribute("city", "");
        model.addAttribute("deviceInfo", "");
        model.addAttribute("operation", "");
        model.addAttribute("firstVisit", "");

        Date now = new Date();
        model.addAttribute("startDate", DateUtil.beginOfDay(now));
        model.addAttribute("endDate", DateUtil.endOfDay(now));
        return "log-records"; // 返回 Thymeleaf 模板
    }

    @RateLimit(ipLimit = 5, globalLimit = 100)
    @GetMapping("/api/log-records")
    @ResponseBody
    public Map<String, Object> getLogRecords(
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String deviceInfo,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String firstVisit,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        List<LogRecord> logRecords;
        long totalItems = 0;

        if (page != null && size != null) {
            // 分页查询
            Pageable pageable = PageRequest.of(page, size);
            Page<LogRecord> logRecordsPage = logRecordService.findAllWithFilters(
                    sessionId, ip, url, country, city, deviceInfo, operation, firstVisit, startDate, endDate, pageable);

            logRecords = logRecordsPage.getContent();
            totalItems = logRecordsPage.getTotalElements();
        } else {
            // 不分页，返回所有数据
            logRecords = logRecordService.findAllWithFilters(
                    sessionId, ip, url, country, city, deviceInfo, operation, firstVisit, startDate, endDate);
            totalItems = logRecords.size();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", logRecords);
        response.put("totalItems", totalItems);

        return response;
    }
}