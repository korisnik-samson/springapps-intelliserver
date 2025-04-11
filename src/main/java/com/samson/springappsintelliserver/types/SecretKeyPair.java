package com.samson.springappsintelliserver.types;


import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Getter
@Setter
public class SecretKeyPair {
    
    private String key;
    private String value;
    
}
