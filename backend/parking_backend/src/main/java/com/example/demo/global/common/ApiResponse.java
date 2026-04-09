package com.example.demo.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success; // 요청이 성공 or 실패 했는지 나타냄
    private String message; // 전달할 설명글
    private T data; // 내용물, T: 제네릭, 어떤타입의 객체든 사용가능

    //성공 응답용 간편 메서드
    //성공했다는 사실과 메시지만 보낼때 사용
    public static <T> ApiResponse<T> success(String message){
        return new ApiResponse<>(true,message,null);
    }

    // 성공 메시지와 함께 실제 데이터(결과값)을 같이 보낼때 사용
    public static <T> ApiResponse<T> success(String message,T data){
        return new ApiResponse<>(true,message,data);
    }
}
