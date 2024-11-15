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

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public S3Service(@Value("${aws.accessKeyId}") String accessKeyId, @Value("${aws.secretKey}") String secretKey, @Value("${aws.s3.region}") String region)
    {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(accessKeyId, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
                .build();
    }

    public void uploadFile(MultipartFile multipartFile, String name)
    {
        if(multipartFile.isEmpty())
        {
            throw new IllegalArgumentException("File is empty");
        }
        File renamedFile = null;
        try
        {
            File file = convertMultiPartFileToFile(multipartFile);
            renamedFile = new File(file.getParent(), name + ".jpg");  // Appending .jpg, adjust as needed
            if (file.renameTo(renamedFile))
            {
                System.out.println("File renamed to: " + renamedFile.getName());
            }
            else
            {
                System.out.println("Failed to rename the file.");
                renamedFile = file;  // Fallback to original if rename fails
            }

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(name + ".jpg")
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromFile(renamedFile));
        }
        catch (S3Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            // Clean up the temporary file
            if (renamedFile != null && renamedFile.exists())
            {
                if (renamedFile.delete())
                {
                    System.out.println("Temporary file deleted successfully.");
                }
                else
                {
                    System.out.println("Failed to delete the temporary file.");
                }
            }
        }
    }
    private File convertMultiPartFileToFile(MultipartFile file)
    {
        File convFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());
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

