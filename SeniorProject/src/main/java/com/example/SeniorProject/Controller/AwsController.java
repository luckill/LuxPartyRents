package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import com.example.SeniorProject.Service.SecretsManagerService;

@RestController
public class AwsController
{
    @Autowired
    private S3Service s3Service;

    @Autowired
    private SecretsManagerService secretsManagerService;
    // New method to store an API key in AWS Secrets Manager
    @PostMapping("/generateAndStoreApiKey")
    public ResponseEntity<String> generateAndStoreApiKey(@RequestParam String secretName) {
        // Generate the API key
        String generatedApiKey = secretsManagerService.generateApiKey();

        // Store the generated API key in AWS Secrets Manager
        String secretArn = secretsManagerService.createSecret(secretName, generatedApiKey);

        // Return the API key and the ARN of the stored secret
        return ResponseEntity.ok("API key: " + generatedApiKey + " stored in Secrets Manager with ARN: " + secretArn);
    }
    @GetMapping("/getApiKey")
    public ResponseEntity<String> getApiKey(@RequestParam String secretName)
    {
        // Retrieve the API key (or secret value) from AWS Secrets Manager
        String apiKey = secretsManagerService.getSecretValue(secretName);

        // Use the API key for some purpose (e.g., making an external API call)
        // For this example, we're simply returning the API key as the response.
        return ResponseEntity.ok("Retrieved API key: " + apiKey);
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<?> uploadFile(@RequestParam MultipartFile file, @RequestParam String name)
    {
        if(file == null || file.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error!!! -File not found.");
        }
        s3Service.uploadFile(file, name);
        return ResponseEntity.ok().body("Picture uploaded successfully.");
    }
}
