package com.example.SeniorProject.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

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

    public void uploadOrderInvoice(File file, int orderId)
    {
        String name = "invoice_" + orderId + ".pdf";
        uploadFileToS3Bucket(file, name);
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

            uploadFileToS3Bucket(renamedFile, renamedFile.getName());
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
    File convertMultiPartFileToFile(MultipartFile file)
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
    void uploadFileToS3Bucket(File file, String name)
    {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(name)
                .acl(ObjectCannedACL.PUBLIC_READ)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
    }

    public File downloadPdfFileFromS3Bucket(String fileName) throws IOException
    {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        // Downloading the file as bytes
        ResponseBytes<?> s3ObjectBytes = s3Client.getObjectAsBytes(getObjectRequest);

        // Ensure the file is saved with a .pdf extension
        File tempFile = File.createTempFile("downloaded-", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(s3ObjectBytes.asByteArray());
        }
        return tempFile;
    }

    public String deleteFileFromS3Bucket(String fileName)
    {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .build();

            s3Client.deleteObject(deleteObjectRequest);
            return "File deleted successfully from S3 bucket";
    }
}

