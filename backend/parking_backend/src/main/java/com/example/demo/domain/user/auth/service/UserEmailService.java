package com.example.demo.domain.user.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserEmailService {


    private final JavaMailSender mailSender;
    private final UserVerificationService userVerificationService;

    public void sendVerificationEmail(String toEmail) {
        String code = String.format("%06d", new Random().nextInt(1000000));

        userVerificationService.saveCode(toEmail,code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[Parking] 비밀번호 재설정 인증번호입니다.");
        message.setText("인증번호는 [ "+code+" ] 입니다. \n3분 이내에 입력해주세요.");

        mailSender.send(message);
    }
}
