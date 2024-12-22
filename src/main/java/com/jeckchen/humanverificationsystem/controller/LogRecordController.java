package com.jeckchen.humanverificationsystem.controller;

import cn.hutool.core.date.DateUtil;
import com.jeckchen.humanverificationsystem.config.VirtualThreadExecutor;
import com.jeckchen.humanverificationsystem.config.rateLimit.RateLimit;
import com.jeckchen.humanverificationsystem.pojo.LogRecord;
import com.jeckchen.humanverificationsystem.service.LogAccessService;
import com.jeckchen.humanverificationsystem.service.LogRecordService;
import com.jeckchen.humanverificationsystem.service.VerificationService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Controller
public class LogRecordController {

    ExecutorService executor = VirtualThreadExecutor.getInstance();

    @Resource
    private VerificationService verificationService;

    @Resource
    private LogRecordService logRecordService;

    @Resource
    private LogAccessService logAccessService;

    @RateLimit(ipLimit = 5, globalLimit = 100)
    @GetMapping("/log")
    public String showLogRecordsPage(Model model, HttpServletRequest request) {
        CompletableFuture.runAsync(() -> logAccessService.logAccess(request, "/log"), executor);
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
    @RequestMapping("/api/log-records")
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
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {



        List<LogRecord> logRecords;
        long totalItems = 0;

        if (page != null && size != null) {
            // 分页查询
            Pageable pageable = PageRequest.of(page, size);
            Page<LogRecord> logRecordsPage = logRecordService.findAllWithFilters(
                    sessionId, ip, url, country, city, deviceInfo, operation, firstVisit, parseDateTime(startDate), parseDateTime(endDate), pageable);

            logRecords = logRecordsPage.getContent();
            totalItems = logRecordsPage.getTotalElements();
        } else {
            // 不分页，返回所有数据
            logRecords = logRecordService.findAllWithFilters(
                    sessionId, ip, url, country, city, deviceInfo, operation, firstVisit, parseDateTime(startDate), parseDateTime(endDate));
            totalItems = logRecords.size();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", logRecords);
        response.put("totalItems", totalItems);

        return response;
    }

    /**
     * 将 yyyy-MM-dd HH:mm 格式的字符串转换为 LocalDateTime
     *
     * @param dateTimeString 要转换的日期时间字符串
     * @return 转换后的 LocalDateTime 对象
     * @throws DateTimeParseException 如果字符串格式不正确，抛出此异常
     */
    public static LocalDateTime parseDateTime(String dateTimeString) throws DateTimeParseException {
        if (StringUtils.isBlank(dateTimeString)){
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTimeString, formatter);
    }
}