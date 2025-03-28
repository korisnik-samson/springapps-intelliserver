package com.samson.springappsintelliserver.types;

import jakarta.persistence.Entity;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Token {
    
    private String token;
    
}
