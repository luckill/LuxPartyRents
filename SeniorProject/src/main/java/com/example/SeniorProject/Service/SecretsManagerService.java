package com.example.SeniorProject.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.*;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class SecretsManagerService
{

    private final SecretsManagerClient secretsManagerClient;

    public SecretsManagerService(@Value("${aws.accessKeyId}") String accessKeyId,
                                 @Value("${aws.secretKey}") String secretKey)
    {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretKey);

        this.secretsManagerClient = SecretsManagerClient.builder()
                .region(Region.US_WEST_1)  // Adjust region according to your setup
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .build();
    }

    // Method to generate a secure API key
    public String generateApiKey()
    {
        // Generate a secure random 24-byte key and encode it as a Base64 string
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[24]; // 24 bytes -> 192 bits
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);  // URL-safe Base64 encoding
    }

    // Method to store the API key in AWS Secrets Manager
    public String createSecret(String secretName, String secretValue)
    {
        try {
            CreateSecretRequest createSecretRequest = CreateSecretRequest.builder()
                    .name(secretName)
                    .secretString(secretValue)
                    .build();

            CreateSecretResponse createSecretResponse = secretsManagerClient.createSecret(createSecretRequest);

            return createSecretResponse.arn();  // Return the ARN of the created secret
        } catch (SecretsManagerException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error while storing the secret in AWS Secrets Manager", e);
        }
    }

    public String getSecretValue(String secretName)
    {
        try {
            // Build the request to get the secret value
            GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                    .secretId(secretName)  // Name or ARN of the secret
                    .build();

            // Fetch the secret value
            GetSecretValueResponse getSecretValueResponse = secretsManagerClient.getSecretValue(getSecretValueRequest);

            // Return the secret value
            return getSecretValueResponse.secretString();
        } catch (SecretsManagerException e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error while retrieving the secret from AWS Secrets Manager", e);
        }
    }

}
