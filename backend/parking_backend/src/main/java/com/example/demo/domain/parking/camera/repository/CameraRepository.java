package com.example.demo.domain.parking.camera.repository;

import com.example.demo.domain.parking.camera.Camera;
import com.example.demo.domain.parking.camera.enums.CameraType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CameraRepository extends JpaRepository<Camera,Long> {
    Optional<Camera> findByCameraCode(String cameraCode);
    List<Camera> findAllByCameraType(CameraType cameraType);
}
