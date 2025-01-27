package com.jeckchen.humanverificationsystem.controller;


import com.jeckchen.humanverificationsystem.config.VirtualThreadExecutor;
import com.jeckchen.humanverificationsystem.config.rateLimit.RateLimit;
import com.jeckchen.humanverificationsystem.pojo.VerificationRequest;
import com.jeckchen.humanverificationsystem.service.LogAccessService;
import com.jeckchen.humanverificationsystem.service.VerificationService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;


@Controller
public class VerificationController {

    ExecutorService executor = VirtualThreadExecutor.getInstance();

    @Resource
    private VerificationService verificationService;

    @Resource
    private LogAccessService logAccessService;

    @RateLimit(ipLimit = 5, globalLimit = 100)
    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        CompletableFuture.runAsync(() -> logAccessService.logAccess(request), executor);
        return "index";
    }

    @RateLimit(ipLimit = 5, globalLimit = 100)
    @GetMapping("/getQuestion")
    @ResponseBody
    public Map<String, Object> getQuestion(HttpServletRequest request) {
        VerificationService.Question question = verificationService.generateQuestion();
        Map<String, Object> response = new HashMap<>();
        response.put("questionText", question.getText());
        response.put("options", question.getOptions());
        return response;
    }

    @RateLimit(ipLimit = 5, globalLimit = 100)
    @PostMapping("/verify")
    @ResponseBody
    public Map<String, Object> verify(@RequestParam String answer, @RequestParam String questionText, HttpServletRequest request) {
        VerificationRequest verificationRequest = new VerificationRequest(questionText, answer);
        boolean isCorrect = verificationService.verifyAnswer(verificationRequest);
        CompletableFuture.runAsync(() -> logAccessService.logVerification(request, verificationRequest, isCorrect), executor);
        Map<String, Object> response = new HashMap<>();
        response.put("correct", isCorrect);
        response.put("redirectUrl", logAccessService.getRedirectUrl());
        return response;
    }
}