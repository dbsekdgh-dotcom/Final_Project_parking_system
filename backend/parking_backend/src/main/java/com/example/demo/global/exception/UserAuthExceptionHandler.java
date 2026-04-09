package com.example.demo.global.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice(basePackages = "com.example.demo.domain.user.auth")
public class UserAuthExceptionHandler {

    @ExceptionHandler(AuthException.class) // 어떤 예외를 잡을지 명시해야 함
    public ResponseEntity<UserErrorResponse> handleAuthException(AuthException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.error("[UserAuth Error] Code: {}, Message: {}", errorCode.name(), errorCode.getMessage());


        return ResponseEntity
                .status(errorCode.getStatus())
                .body(UserErrorResponse.builder()
                        .status(errorCode.getStatus().value())
                        .code(errorCode.name())
                        .message(errorCode.getMessage())
                        .build());
    }

    /**
     * 리액트에게 전달할 우리만의 에러 응답 규격입니다.
     * 이름 충돌을 피하기 위해 UserErrorResponse로 지었습니다.
     */
    @Getter
    @Builder
    public static class UserErrorResponse {
        private final int status;
        private final String code;
        private final String message;
    }

    /**
     * @Valid 검증 실패 시 발생하는 예외를 잡습니다.
     * (이메일 미입력 등)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<UserErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        // DTO의 @NotBlank(message = "...")에 적힌 메시지를 가져옵니다.
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        log.error("[Validation Error] Message: {}", errorMessage);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 검증 실패는 보통 400 에러를 줍니다.
                .body(UserErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .code("VALIDATION_ERROR")
                        .message(errorMessage) // "이메일을 입력해주세요."가 담깁니다.
                        .build());
    }
}