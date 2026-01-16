package com.ptit.library.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {
    private String fullName;
    private String dateOfBirth;
    private String gender;
    private String phone;
    private String address;
    private String major;
}
