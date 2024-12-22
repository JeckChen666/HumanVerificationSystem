package com.jeckchen.humanverificationsystem.service;

import com.jeckchen.humanverificationsystem.config.AppConfig;
import com.jeckchen.humanverificationsystem.pojo.LogRecord;
import com.jeckchen.humanverificationsystem.pojo.VerificationRequest;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
public class VerificationService{

    private static final String[] symbols = {"+", "-"};

    @Resource
    private LogService logService;

    @Resource
    private AppConfig appConfig;

    public Question generateQuestion() {
        int result;
        int num1;
        int num2;
        Random random;
        int symbolIndex;
        do {
            random = new Random();
            num1 = random.nextInt(100);
            num2 = random.nextInt(100);
            symbolIndex = random.nextInt(2);
            if (num1 < num2) {
                int temp = num1;
                num1 = num2;
                num2 = temp;
            }
            if (0 == symbolIndex) {
                result = num1 + num2;
            } else {
                result = num1 - num2;
            }
        } while (result > 100);
        String questionText = num1 + " " + (symbols[symbolIndex]) + " " + num2;
        List<Integer> options = generateOptions(result);
        return new Question(questionText, options);
    }

    private List<Integer> generateOptions(int result) {
        List<Integer> options = new ArrayList<>();
        options.add(result);
        while (options.size() < 4) {
            int option = result + (int) (Math.random() * 20 - 10); // Generate options within a range of +/- 10
            if (option > 100 || option < 0) {
                continue;
            }
            if (!options.contains(option)) {
                options.add(option);
            }
        }
        Collections.shuffle(options);
        return options;
    }

    public boolean verifyAnswer(VerificationRequest verificationRequest) {
        String[] parts = verificationRequest.getQuestion()
                                            .split(" ");
        int num1 = Integer.parseInt(parts[0]);
        int num2 = Integer.parseInt(parts[2]);
        int result = parts[1].equals("+") ? num1 + num2 : num1 - num2;
        int userAnswer = Integer.parseInt(verificationRequest.getAnswer());
        return userAnswer == result;
    }

    @Getter
    public static class Question {
        private String text;
        private List<Integer> options;

        public Question(String text, List<Integer> options) {
            this.text = text;
            this.options = options;
        }

    }

}