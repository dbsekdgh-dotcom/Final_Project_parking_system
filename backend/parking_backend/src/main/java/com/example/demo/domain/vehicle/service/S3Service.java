package com.example.demo.domain.vehicle.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;
// 테스트용 주석
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file) {

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload.jpg";
        String fileName = UUID.randomUUID() + "-" + originalFilename;

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(resolveContentType(file));

            amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata));

            // ACL 없이 업로드 후 5분짜리 pre-signed URL 생성 (Naver OCR API 호출용)
            Date expiration = new Date(System.currentTimeMillis() + 5 * 60 * 1000);
            GeneratePresignedUrlRequest presignedRequest =
                    new GeneratePresignedUrlRequest(bucket, fileName, HttpMethod.GET)
                            .withExpiration(expiration);

            return amazonS3.generatePresignedUrl(presignedRequest).toString();
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
    }

    private String resolveContentType(MultipartFile file) {
        String contentType = file.getContentType();
        // 브라우저가 content-type을 제대로 안 보내는 경우 파일명으로 추론
        if (contentType == null || contentType.isBlank() || contentType.equals("application/octet-stream")) {
            String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
            if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
            if (name.endsWith(".png"))  return "image/png";
            if (name.endsWith(".pdf"))  return "application/pdf";
            if (name.endsWith(".tiff") || name.endsWith(".tif")) return "image/tiff";
            return "image/jpeg"; // 기본값
        }
        // image/jpg → image/jpeg 정규화 (일부 브라우저 비표준 타입)
        if (contentType.equalsIgnoreCase("image/jpg")) return "image/jpeg";
        return contentType;
    }

    public void deleteFile(String fileName) {
        try {
            amazonS3.deleteObject(bucket, fileName);
            log.info("--- [S3Service] S3 파일 삭제 완료: {} ---", fileName);
        } catch (Exception e) {
            log.error("--- [S3Service] S3 파일 삭제 중 에러 발생: {} ---", e.getMessage());
            throw new RuntimeException("S3 파일 삭제 실패", e);
        }
    }

}
