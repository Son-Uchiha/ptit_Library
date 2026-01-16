package com.ptit.library.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private Integer id;
    private String bookCode;
    private String title;
    private String author;
    private Integer publishedYear;
    private Integer totalCopies;
    private Integer copiesAvailable;
    private String publisher;
}
