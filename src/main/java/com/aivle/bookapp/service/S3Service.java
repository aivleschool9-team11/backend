package com.aivle.bookapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // CloudFront 도메인 (시크릿 아님). yml에 값 없어도 기본값으로 CloudFront URL 반환되도록
    // 기본값을 박아둔다 → 팀원별 application.yml 설정 누락으로 S3 URL이 저장되는 드리프트 방지.
    @Value("${cloud.aws.cloudfront.domain:d1i18e14fwa1wz.cloudfront.net}")
    private String cloudFrontDomain;

    /**
     * 이미지 파일을 S3에 업로드하고 접근 가능한 URL을 반환한다.
     */
    public String upload(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.'))
                : "";
        String key = "images/" + UUID.randomUUID() + ext;

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request,
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        // CloudFront 도메인이 설정돼 있으면 CDN URL, 없으면 S3 URL 반환
        if (cloudFrontDomain != null && !cloudFrontDomain.isBlank()) {
            return "https://" + cloudFrontDomain + "/" + key;
        }
        return s3Client.utilities()
                .getUrl(GetUrlRequest.builder().bucket(bucket).key(key).build())
                .toString();
    }
}
