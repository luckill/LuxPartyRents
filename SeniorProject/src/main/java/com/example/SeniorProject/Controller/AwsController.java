package com.example.SeniorProject.Controller;

import com.example.SeniorProject.Service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import com.example.SeniorProject.Service.SecretsManagerService;
import org.springframework.web.server.ResponseStatusException;

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
        try
        {
            String generatedApiKey = secretsManagerService.generateApiKey();
            String secretArn = secretsManagerService.createSecret(secretName, generatedApiKey);

            return ResponseEntity.ok("API key: " + generatedApiKey + " stored in Secrets Manager with ARN: " + secretArn);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }
    @GetMapping("/getApiKey")
    public ResponseEntity<String> getApiKey(@RequestParam String secretName)
    {
        try
        {
            String apiKey = secretsManagerService.getSecretValue(secretName);

            // Use the API key for some purpose (e.g., making an external API call)
            // For this example, we're simply returning the API key as the response.
            return ResponseEntity.ok("Retrieved API key: " + apiKey);
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<?> uploadFile(@RequestParam MultipartFile file, @RequestParam String name)
    {
        try
        {
            if(file == null || file.isEmpty())
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error!!! -File not found.");
            }
            s3Service.uploadFile(file, name);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        catch (ResponseStatusException exception)
        {
            return ResponseEntity.status(exception.getStatusCode()).body(exception.getReason());
        }
    }

    @GetMapping("/healthcheck")
    public ResponseEntity<?> healthCheck()
    {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
