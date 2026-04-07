package com.example.demo.domain.shared.camera.repository;

import com.example.demo.domain.shared.camera.Camera;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CameraRepository extends JpaRepository<Camera,Long> {
}
