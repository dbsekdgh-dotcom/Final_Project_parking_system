package com.example.demo.domain.kiosk.entry.service;

import com.example.demo.domain.kiosk.entry.dtos.response.OcrResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AiClient {
    private final WebClient webClient;

    public AiClient(){
        this.webClient=WebClient.builder()
                .baseUrl("http://localhost:8000")
                .build();
    }
    public OcrResponse requestOcr(MultipartFile file){
        return webClient.post()
                .uri("/entryexit")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(
                        BodyInserters.fromMultipartData(
                                "file",
                                file.getResource()
                        )
                )
                .retrieve()
                .bodyToMono(OcrResponse.class)
                .block();
    }
}
