package com.ptit.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordResponse {
    private Integer id;
    private Integer recordId;
    private String borrowCode;
    private String title;
    private String author;
    private Integer year;
    private LocalDate registerDate;
    private LocalDate dueDate;
    private String status;
    private String username;
    private String avatar;
}
