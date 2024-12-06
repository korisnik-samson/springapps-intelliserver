package com.samson.springappsintelliserver.providers;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PasswordProvider {

    String currentPassword;
    String newPassword;

}
