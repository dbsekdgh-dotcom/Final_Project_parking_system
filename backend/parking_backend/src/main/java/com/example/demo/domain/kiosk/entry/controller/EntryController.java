package com.example.demo.domain.kiosk.entry.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping("/kiosk")
public class EntryController {

    @PostMapping("/entry")
    public ResponseEntity<Void> entry(@RequestParam MultipartFile file){

    }

}
