package com.example.demo.domain.kiosk.entry.dtos.response;

import lombok.Getter;

@Getter
public class OcrResponse {
    private String plateNumber;
    private String s3path;
}
