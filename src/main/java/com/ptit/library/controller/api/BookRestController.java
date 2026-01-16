package com.ptit.library.controller.api;

import com.ptit.library.dto.response.ApiResponse;
import com.ptit.library.dto.response.BookResponse;
import com.ptit.library.model.Book;
import com.ptit.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
public class BookRestController {

    @Autowired
    private BookService bookService;

    /**
     * GET /api/books
     * Lấy danh sách tất cả sách
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookResponse>>> getAllBooks() {
        List<Book> books = bookService.findAll();
        List<BookResponse> responses = books.stream()
                .map(this::toBookResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * GET /api/books/{id}
     * Lấy thông tin chi tiết một cuốn sách
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(@PathVariable Integer id) {
        Optional<Book> bookOpt = bookService.findById(id);

        if (bookOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ApiResponse.success(toBookResponse(bookOpt.get())));
    }

    /**
     * GET /api/books/search
     * Tìm kiếm sách theo từ khóa và bộ lọc
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<BookResponse>>> searchBooks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "") String filter1,
            @RequestParam(defaultValue = "") String filter2,
            @RequestParam(defaultValue = "") String filter3) {

        List<Book> books = bookService.searchBooks(keyword, filter1, filter2, filter3);
        List<BookResponse> responses = books.stream()
                .map(this::toBookResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    private BookResponse toBookResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .bookCode(book.getBookCode())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publishedYear(book.getPublishedYear())
                .totalCopies(book.getTotalCopies())
                .copiesAvailable(book.getCopiesAvailable())
                .publisher(book.getPublisher())
                .build();
    }
}
