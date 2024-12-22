package com.jeckchen.humanverificationsystem.service;

import com.jeckchen.humanverificationsystem.config.AppConfig;
import com.jeckchen.humanverificationsystem.pojo.VerificationRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

/**
 * @author JeckChen
 * @version 1.0.0
 * @className LogAccessService.java
 * @description
 * @date 2024年12月21日 14:29
 */
@Service
public class LogAccessService {

    @Resource
    private LogService logService;

    @Resource
    private AppConfig appConfig;

    public void logAccess(HttpServletRequest request) {
        logService.logAccess(request);
    }

    public void logAccess(HttpServletRequest request, String pageName) {
        logService.logAccess(request, pageName);
    }

    public void logAccess(HttpServletRequest request, String pageName, String originalUrl) {
        logService.logAccess(request, pageName, originalUrl);
    }

    public void logVerification(HttpServletRequest request, VerificationRequest verificationRequest, boolean isCorrect) {
        logService.logVerification(request, verificationRequest, isCorrect);
    }

    public String getRedirectUrl() {
        return appConfig.getRedirectUrl();
    }
}
