package com.example.demo.domain.kiosk.entry.repository;

import com.example.demo.domain.shared.camera.Camera;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EntryCameraRepository extends JpaRepository<Camera,Long> {
    Optional<Camera> findByCameraCode(String cameraCode);
}
