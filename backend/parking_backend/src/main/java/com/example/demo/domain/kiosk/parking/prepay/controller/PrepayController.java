package com.example.demo.domain.kiosk.parking.prepay.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/prepays")
@CrossOrigin(origins = "http://localhost:5173") // cors잠시 해제, 시큐리티 완성시 삭제
public class PrepayController {
    @PostMapping("/search-car")
    public List<String> searchPrepayCar(@RequestBody String carNumber){
        System.out.println(carNumber);
        List<String> carList = Arrays.asList("12가 3456", "12가 3457", "12가 3458");
        return carList;
    }
}
