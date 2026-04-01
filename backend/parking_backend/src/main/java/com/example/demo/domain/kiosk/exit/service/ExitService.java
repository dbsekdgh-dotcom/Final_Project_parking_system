package com.example.demo.domain.kiosk.exit.service;

import com.example.demo.domain.shared.parkinglog.ParkingLog;
import com.example.demo.domain.kiosk.exit.repository.ExitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExitService {
    private final ExitRepository exitRepository;

    public List<ParkingLog> testlog(){
       return exitRepository.findAll();
    }
}
