package com.example.demo.domain.user.vehicle.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file) {

        String fileName = UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));

            return amazonS3.getUrl(bucket, fileName).toString();
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }
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
