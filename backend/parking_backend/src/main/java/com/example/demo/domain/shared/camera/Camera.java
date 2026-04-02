package com.example.demo.domain.shared.camera;

import com.example.demo.domain.shared.camera.enums.CameraType;
import com.example.demo.domain.shared.camera.enums.Floor;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "camera")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Camera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "camera_id")
    private Long id;

    @Column(name = "camera_code", length = 100, unique = true, nullable = false)
    @Comment("카메라 식별 코드")
    private String cameraCode;

    @Lob // SQL의 TEXT 타입 매핑
    @Column(name = "description", columnDefinition = "TEXT")
    @Comment("카메라 상세 설명")
    private String description;

    @Column(name = "location", length = 200, nullable = false)
    @Comment("설치 위치 (예: 정문 게이트)")
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "floor", nullable = false)
    @Comment("설치 층 (B1, B2)")
    private Floor floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "camera_type", nullable = false)
    @Comment("카메라 타입 (ENTRY, EXIT, AREA)")
    private CameraType cameraType;

    @Column(name = "rtsp_url", length = 500)
    @Comment("스트리밍 주소 (더미 데이터용)")
    private String rtspUrl;
}