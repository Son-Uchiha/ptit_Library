package com.ptit.library.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponse {
    private boolean valid;
    private String usernameError;
    private String emailError;
    private String passwordError;
    private String retypePasswordError;
    private String globalMessage;
}
