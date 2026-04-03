package com.example.demo.global.controller;

import com.example.demo.global.exception.BusinessException;
import com.example.demo.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.demo.global.response.ErrorResponse;

//@RestControllerAdvice :전역 예외 처리기(모든 컨트롤러의 예외를 여기서 처리)
//react에 다음과 같이 json으로 전송
//{
//  "code": "ALREADY_EXITED",
//  "message": "이미 출차가 완료된 차량입니다."
//}
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    //BusinessException.class에서 정의한 에러를 처리
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
        log.error("정의되지 않은 서버 에러",e);

        //react에 반환할 내용
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR.name(),ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}
