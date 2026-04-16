package com.example.demo.domain.user.vehicle.dtos.response;

import lombok.Getter;
import java.util.List;

/**
 * [최상위 클래스] 네이버 클로바 OCR API의 전체 응답을 담는 객체
 */
@Getter
public class NaverOcrResponse {
    private final List<ImageResponse> images;

    public NaverOcrResponse(List<ImageResponse> images) {
        this.images = images;
    }

    @Getter
    public static class ImageResponse {
        private final String inferResult;
        private final List<FieldResponse> fields;

        public ImageResponse(String inferResult, List<FieldResponse> fields) {
            this.inferResult = inferResult;
            this.fields = fields;
        }
    }

    @Getter
    public static class FieldResponse {
        private final String name;
        private final String inferText;
        private final Double inferConfidence;

        public FieldResponse(String name, String inferText, Double inferConfidence) {
            this.name = name;
            this.inferText = inferText;
            this.inferConfidence = inferConfidence;
        }
    }
}
