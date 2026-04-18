package com.example.demo.domain.vehicle.dtos.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.util.List;

/**
 * [최상위 클래스] 네이버 클로바 OCR API의 전체 응답을 담는 객체
 */
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverOcrResponse {

    private final List<ImageResponse> images;

    @JsonCreator
    public NaverOcrResponse(@JsonProperty("images") List<ImageResponse> images) {
        this.images = images;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageResponse {

        private final String inferResult;
        private final List<FieldResponse> fields;

        @JsonCreator
        public ImageResponse(
                @JsonProperty("inferResult") String inferResult,
                @JsonProperty("fields") List<FieldResponse> fields) {
            this.inferResult = inferResult;
            this.fields = fields;
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FieldResponse {

        private final String name;
        private final String inferText;
        private final Double inferConfidence;

        @JsonCreator
        public FieldResponse(
                @JsonProperty("name") String name,
                @JsonProperty("inferText") String inferText,
                @JsonProperty("inferConfidence") Double inferConfidence) {
            this.name = name;
            this.inferText = inferText;
            this.inferConfidence = inferConfidence;
        }
    }
}
