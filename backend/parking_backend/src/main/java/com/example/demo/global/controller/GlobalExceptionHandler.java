package com.example.demo.global.controller;

import com.example.demo.global.exception.AuthException;
import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.demo.global.response.ErrorResponse;
import org.springframework.web.servlet.resource.NoResourceFoundException;

//@RestControllerAdvice :전역 예외 처리기(모든 컨트롤러의 예외를 여기서 처리)
//react에 다음과 같이 json으로 전송
//{
//  "code": "ALREADY_EXITED",
//  "message": "이미 출차가 완료된 차량입니다."
//}
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외 처리 - BusinessException 타입의 예외가 발생했을 때만 작동
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handlerBusinessException(BusinessException e){
        ErrorCode errorCode=e.getErrorCode();

        //에러 응답 객체 생성
        ErrorResponse response=new ErrorResponse(
                errorCode.name(), //ALREADY_EXITED
                errorCode.getMessage() //이미 출차가 완료된 차량입니다.
        );
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    //상기 오류 외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handlerException(Exception e){
        //에러 수정을 위한 로그 메세지 출력
        //에러 스택트레이스 출력 - 발생한 에러의 원인과 과정(Stack Trace)을 서버 콘솔에 상세하게 기록함
        log.error("정의되지 않은 서버 에러",e);

        //react에 반환할 내용
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.name(),ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handlerAuthException(AuthException e) {
        ErrorCode errorCode = e.getErrorCode();

        ErrorResponse response = new ErrorResponse(
                errorCode.name(),
                errorCode.getMessage()
        );

        log.warn("인증 예외 발생: {}", errorCode.name());

        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
    // 1. 패키지 경로에 맞춰서 import 확인하세요! (예: com.example.demo.global.exception.CustomException)
    @ExceptionHandler(com.example.demo.global.exception.CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(com.example.demo.global.exception.CustomException e) {
        log.warn("### [비즈니스 예외] 커스텀 에러 발생: {}", e.getMessage());

        // ErrorResponse 규격에 맞춰서 반환
        ErrorResponse response = new ErrorResponse(
                "BUSINESS_ERROR", // 프론트에서 구분할 코드명
                e.getMessage()    // "해당 세대는 이미 입주가 완료되었습니다."
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // DTO 검증 오류 처리 (@Valid, @Min, @NotBlank 등)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e){
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        // e.getBindingResult(): 검증 결과표, 어디가 틀렸고, 왜 틀렸는지 정보가 담겨있음
        // .getAllErrors(): 발생한 모든 에러 리스트를 가져옴
        // .get(0): 그 중 가장 첫번째 에러를 선택함
        // .getDefaultMessage(): DTO에 적어둔 message="할인 금액은 0원 이상이어야 합니다" 텍스트를 뽑아냄
        log.error("Validation failed: {}",errorMessage);

        ErrorResponse response = new ErrorResponse(
                "INVALID_INPUT", //리액트에서 구분할 에러 코드명
                errorMessage //DTO에 적힌 "할인 금액은 0원 이상이어야 합니다"
        );

        // 400 Bad Request로 응답
        return ResponseEntity.status(HttpStatus.BAD_REQUEST) // HTTP응담 상태 코드를 400(Bad Request)로 설정함
                .body(response); // ApiResponse 규격에 맞게 데이터를 담아줌
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("데이터 무결성 위반: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLICT", "이미 존재하는 데이터입니다."));
    }
}
