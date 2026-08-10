package com.playbit.backend.s3;

import com.playbit.backend.common.ErrorCode;
import com.playbit.backend.common.exception.BadRequestException;
import com.playbit.backend.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {

    // 스프링 부트 버전 문제 때문에 S3Template 대신 순수 AWS SDK 객체인 S3Client를 주입
    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    /**
     * 파일을 S3에 업로드하고 접근 가능한 URL을 반환합니다.
     */
    public String uploadImage(MultipartFile image, String directory) {
        if (image == null || image.isEmpty()) {
            throw new NotFoundException(ErrorCode.IMAGE_NOT_FOUND);
        }

        try {
            // 원본 파일명에서 확장자 추출 및 고유한 파일명 생성
            String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
            String uniqueFilename = directory + "/" + UUID.randomUUID() + "." + extension;

            // 업로드 요청 객체 조립
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(uniqueFilename)
                    .contentType(image.getContentType())
                    .build();

            // S3Client를 통해 S3로 파일 스트림 전송
            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(image.getInputStream(), image.getSize()));

            // 업로드 완료 후, 접속 가능한 이미지 주소(URL)를 수동으로 조합하여 반환
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, uniqueFilename);

        } catch (IOException e) {
            log.error("S3 파일 업로드 중 오류 발생", e);
            throw new BadRequestException(ErrorCode.IMAGE_UPLOAD_FAIL);
        }
    }
}