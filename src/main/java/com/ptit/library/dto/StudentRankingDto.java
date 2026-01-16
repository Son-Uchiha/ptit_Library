package com.ptit.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentRankingDto {
    private String studentCode;
    private String fullName;
    private String enrollmentYear;
    private String major;
    private Long borrowCount;
    private Boolean online;
}
