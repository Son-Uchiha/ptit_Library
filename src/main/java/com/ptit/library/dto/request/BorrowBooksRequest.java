package com.ptit.library.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowBooksRequest {

    @NotEmpty(message = "Danh sách sách mượn không được để trống")
    private List<Integer> bookIds;
}
