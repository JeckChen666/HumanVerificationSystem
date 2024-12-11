package com.jeckchen.humanverificationsystem.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VerificationRequest {
    private String question;
    private String answer;

    public VerificationRequest(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }
}