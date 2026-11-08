
package com.backend.ecorruta.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class ConfiguracionStorageNeon {

    @Bean
    public S3Client s3Client(
            @Value("${neon.storage.endpoint}") String endpoint,
            @Value("${neon.storage.region}") String region,
            @Value("${neon.storage.access-key}") String accessKey,
            @Value("${neon.storage.secret-key}") String secretKey) {

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        accessKey,
                                        secretKey)))
                .forcePathStyle(true)
                .build();
    }
}
