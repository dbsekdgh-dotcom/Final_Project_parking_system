package com.example.demo.domain.user.auth.service;

import com.example.demo.domain.shared.user.User;
import com.example.demo.domain.user.auth.repository.UserAuthRepository;
import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFindService {

    private final UserAuthRepository userAuthRepository;

    public String findEmail(String name, String phone) {
        User user = userAuthRepository.findByNameAndPhone(name, phone)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));
        return maskEmail(user.getEmail());
    }

    private String maskEmail(String email) {

        try {
            String[] parts = email.split("@");
            String id = parts[0];
            String domain = parts[1];

            if (id.length() <= 3) {
                return id + "****@" + domain;
            }
            return id.substring(0,3) + "****@"+domain;
        }catch (Exception e) {
            return email;
        }
    }

}
