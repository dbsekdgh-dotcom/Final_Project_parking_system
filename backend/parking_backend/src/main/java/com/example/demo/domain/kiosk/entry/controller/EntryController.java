package com.example.demo.domain.kiosk.entry.controller;

import com.example.demo.domain.kiosk.entry.service.EntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1/entry")
public class EntryController {
    private final EntryService entryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> entry(@RequestPart("file") MultipartFile file) {
        entryService.detectedEntry(file);
        return ResponseEntity.ok().build();
    }

}
