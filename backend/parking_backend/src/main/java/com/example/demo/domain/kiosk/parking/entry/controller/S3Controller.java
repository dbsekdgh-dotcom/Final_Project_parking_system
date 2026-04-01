package com.example.demo.domain.kiosk.parking.entry.controller;


import com.example.demo.domain.kiosk.parking.entry.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/s3")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class S3Controller {

    private final S3Service s3Service;
    @PostMapping("/upload")
    public ResponseEntity<Map<String,String>> uploadFile(@RequestParam("file")MultipartFile file) {
        try{
            String imageUrl = s3Service.uploadFile(file);

            Map<String,String> response = new HashMap<>();
            response.put("url",imageUrl);

            return ResponseEntity.ok(response);


        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

}
