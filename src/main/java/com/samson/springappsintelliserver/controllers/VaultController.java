package com.samson.springappsintelliserver.controllers;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.samson.springappsintelliserver.types.SecretKeyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class VaultController {
    
    private final SecretClient secretClient;

    @Autowired
    public VaultController(SecretClient secretClient) {
        this.secretClient = secretClient;
    }
    
    @GetMapping("/getSecret/{key}")
    public String getSecretFromKeyVault(@PathVariable String key) {
        KeyVaultSecret secret = secretClient.getSecret(key);
        
        return secret.getValue();
    }
    
    @PostMapping("/setSecret")
    public String setSecretInKeyVault(@RequestBody SecretKeyPair secretKey) {
        KeyVaultSecret secret = new KeyVaultSecret(secretKey.getKey(), secretKey.getValue());
        secretClient.setSecret(secret);
        
        return "Secret set successfully!";
    }
    
}
