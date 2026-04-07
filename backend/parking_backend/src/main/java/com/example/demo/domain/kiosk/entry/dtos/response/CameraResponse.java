package com.example.demo.domain.kiosk.entry.dtos.response;

import com.example.demo.domain.shared.camera.Camera;
import lombok.Getter;

@Getter
public class CameraResponse {
    private final Long cameraId;
    private final String cameraCode;
    private final String location;
    private final String description;
    private final String floor;

    public CameraResponse(Camera camera) {
        this.cameraId = camera.getId();
        this.cameraCode = camera.getCameraCode();
        this.location = camera.getLocation();
        this.description = camera.getDescription();
        this.floor = camera.getFloor().name();
    }
}
