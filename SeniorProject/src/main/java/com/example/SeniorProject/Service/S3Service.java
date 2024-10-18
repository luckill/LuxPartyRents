package com.example.SeniorProject.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

@Service
public class S3Service
{
    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service(@Value("${aws.accessKeyId}") String accessKeyId, @Value("${aws.secretKey}") String secretKey, @Value("${aws.s3.region}") String region)
    {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKeyId, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .build();
    }

    public void uploadFile(MultipartFile multipartFile)
    {
        if(multipartFile.isEmpty())
        {
            throw new IllegalArgumentException("File is empty");
        }
        try
        {
            File file = convertMultiPartFileToFile(multipartFile);
            String keyName = Paths.get(multipartFile.getOriginalFilename()).getFileName().toString();
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(keyName)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
            if (file.delete())
            {
                System.out.println("Temporary file deleted successfully.");
            }
            else
            {
                System.out.println("Failed to delete the temporary file.");
            }
        } catch (S3Exception e)
        {
            e.printStackTrace();
        }
    }
    private File convertMultiPartFileToFile(MultipartFile file)
    {
        File convFile = new File(file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile))
        {
            fos.write(file.getBytes());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return convFile;
    }
}

