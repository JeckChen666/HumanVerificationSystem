package com.jeckchen.humanverificationsystem.controller;

import com.jeckchen.humanverificationsystem.service.VerificationService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class CustomErrorController implements ErrorController {

    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Resource
    private VerificationService verificationService;

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        // 获取原始请求地址
        String originalUri = (String) request.getAttribute("jakarta.servlet.error.request_uri");

        // 判断是否为 404 错误
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");

        CompletableFuture.runAsync(() -> verificationService.logAccess(request, originalUri), executor);

        if (statusCode != null && statusCode == HttpServletResponse.SC_NOT_FOUND) {
            return "The requested URL [" + originalUri + "] does not exist. Redirected to custom handler.";
        }

        // 其他错误返回通用信息
        return "An unexpected error occurred.";
    }
}
