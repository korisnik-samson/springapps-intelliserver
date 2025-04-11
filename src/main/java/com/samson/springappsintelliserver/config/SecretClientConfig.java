package com.samson.springappsintelliserver.config;

import com.azure.identity.DefaultAzureCredentialBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;


@Configuration
public class SecretClientConfig {
    @Value("${spring.cloud.azure.keyvault.secret.endpoint}")
    private String secretEndpoint;
    
    @Bean
    public SecretClient getSecretClient() {
        return new SecretClientBuilder()
                .vaultUrl(secretEndpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }
    
}
