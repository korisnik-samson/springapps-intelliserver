package com.samson.springappsintelliserver.services;

import com.samson.springappsintelliserver.utils.Utils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.core.credential.TokenCredential;

@Service
public class JWTService {
    private final String SECRET_KEY;
    private final SecretClient secretClient;

    public JWTService(SecretClient secretClient) {
        this.secretClient = secretClient;

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            SecretKey secretKey = keyGenerator.generateKey();

            this.SECRET_KEY = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            
            // Store the secret key in Azure Key Vault
            KeyVaultSecret secret = new KeyVaultSecret("jwt-secret-key", this.SECRET_KEY);
            this.secretClient.setSecret(secret);

        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();

        // note that the token expires in 30 days
        // note the linux time format is in use... 30 days might be a problem
        // optimal time is 14 days
        return Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + Utils.calculateTime(true, 14)))
                .and().signWith(getKey()).compact();
    }

    @NonNull
    private SecretKey getKey() {
        byte[] key = Decoders.BASE64.decode(this.SECRET_KEY);
        return Keys.hmacShaKeyFor(key);
    }

    public String extractUsername(String token) {
        // extract the username from the token
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, @NonNull Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token).getPayload();
    }

    public boolean validateToken(String token, @NonNull UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
