package com.jeckchen.humanverificationsystem.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VerificationLog {
    private LocalDateTime timestamp;
    private String ip;
    private String region;
    private String deviceInfo;
    private String question;
    private String answer;
    private boolean isCorrect;
}