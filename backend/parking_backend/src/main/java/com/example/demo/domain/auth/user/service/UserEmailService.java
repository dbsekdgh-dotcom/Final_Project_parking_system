package com.example.demo.domain.auth.user.service;

import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEmailService {
    
    private final JavaMailSender mailSender;
    private final UserVerificationService userVerificationService;

    public void sendVerificationEmail(String toEmail) {
        String code = String.format("%06d", new Random().nextInt(1000000));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("[Parking] 비밀번호 재설정 인증번호입니다.");
        message.setText("인증번호는 [ "+code+" ] 입니다. \n3분 이내에 입력해주세요.");

        try {
            mailSender.send(message);
            userVerificationService.saveCode(toEmail, code);
        } catch (Exception e) {
            log.error("이메일 발송 실패 - 수신자: {}, 원인: {}", toEmail, e.getMessage());
            throw new AuthException(ErrorCode.MAIL_SEND_FAILED);
        }
    }
}
