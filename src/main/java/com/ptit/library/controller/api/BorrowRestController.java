package com.ptit.library.controller.api;

import com.ptit.library.dto.request.BorrowBooksRequest;
import com.ptit.library.dto.response.ApiResponse;
import com.ptit.library.dto.response.BorrowRecordResponse;
import com.ptit.library.model.BookRecord;
import com.ptit.library.model.User;
import com.ptit.library.service.BorrowService;
import com.ptit.library.service.UserService;
import com.ptit.library.util.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/borrows")
public class BorrowRestController {

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private UserService userService;

    /**
     * POST /api/borrows
     * Đăng ký mượn sách
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> borrowBooks(
            @Valid @RequestBody BorrowBooksRequest request) {

        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        try {
            int[] bookIds = request.getBookIds().stream().mapToInt(Integer::intValue).toArray();
            List<Integer> failedBooks = borrowService.borrowBooks(username, bookIds);

            Map<String, Object> data = new HashMap<>();
            data.put("borrowedCount", bookIds.length - failedBooks.size());
            data.put("failedBookIds", failedBooks);

            if (failedBooks.isEmpty()) {
                return ResponseEntity.ok(
                        ApiResponse.success(data, "Đăng ký mượn sách thành công"));
            } else {
                return ResponseEntity.ok(
                        ApiResponse.success(data, "Một số sách không thể mượn"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi mượn sách: " + e.getMessage(), 500));
        }
    }

    /**
     * GET /api/borrows/records
     * Lấy danh sách phiếu mượn sách
     */
    @GetMapping("/records")
    public ResponseEntity<ApiResponse<List<BorrowRecordResponse>>> getRecords(
            @RequestParam(required = false) String username) {

        String authenticated = SecurityUtil.getAuthenticatedUsername();
        if (authenticated == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        // If username param is empty, use authenticated username
        String studentId = (username == null || username.isBlank()) ? authenticated : username;

        List<Object[]> records = borrowService.getRecordsWithBookInfo(studentId);
        List<BorrowRecordResponse> responses = convertToResponses(records);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * GET /api/borrows/all
     * Lấy tất cả phiếu mượn sách (Admin only)
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BorrowRecordResponse>>> getAllRecords(
            @RequestParam(required = false) String status) {

        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        // Kiểm tra role admin (DB lưu là ADMIN viết hoa)
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty() || !"ADMIN".equalsIgnoreCase(userOpt.get().getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bạn không có quyền truy cập", 403));
        }

        List<Object[]> records = borrowService.getAllRecordsWithBookInfo(status);
        List<BorrowRecordResponse> responses = convertToResponses(records);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    private List<BorrowRecordResponse> convertToResponses(List<Object[]> records) {
        List<BorrowRecordResponse> responses = new ArrayList<>();

        for (Object[] row : records) {
            Object studentObj = row[0];
            Object recIdObj = row[1];
            Object bookIdObj = row[2];
            Object titleObj = row[3];
            Object authorObj = row[4];
            Object yearObj = row[5];
            Object borrowDateObj = row[6];
            Object statusObj = row[7];

            Integer recId = recIdObj == null ? null : Integer.parseInt(recIdObj.toString());
            Integer pubYear = null;
            if (yearObj != null) {
                try {
                    pubYear = Integer.parseInt(yearObj.toString());
                } catch (Exception ignored) {
                }
            }

            LocalDate borrowLocal = null;
            if (borrowDateObj != null) {
                if (borrowDateObj instanceof java.sql.Date) {
                    borrowLocal = ((java.sql.Date) borrowDateObj).toLocalDate();
                } else if (borrowDateObj instanceof LocalDate) {
                    borrowLocal = (LocalDate) borrowDateObj;
                } else {
                    try {
                        borrowLocal = LocalDate.parse(borrowDateObj.toString());
                    } catch (Exception ignored) {
                    }
                }
            }

            LocalDate dueLocal = borrowLocal != null ? borrowLocal.plusDays(14) : null;

            BorrowRecordResponse response = BorrowRecordResponse.builder()
                    .id(recId)
                    .recordId(recId)
                    .borrowCode(recId != null ? String.valueOf(recId) : "")
                    .title(titleObj != null ? titleObj.toString() : "")
                    .author(authorObj != null ? authorObj.toString() : "")
                    .year(pubYear)
                    .registerDate(borrowLocal)
                    .dueDate(dueLocal)
                    .status(statusObj != null ? statusObj.toString() : "")
                    .username(studentObj != null ? studentObj.toString() : "")
                    .build();

            responses.add(response);
        }

        return responses;
    }

    /**
     * POST /api/borrows/{recordId}/issue
     * Xác nhận cho mượn sách (Admin)
     */
    @PostMapping("/{recordId}/issue")
    public ResponseEntity<ApiResponse<Map<String, Object>>> issueRecord(
            @PathVariable Integer recordId) {

        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        // Kiểm tra role admin (DB lưu là ADMIN viết hoa)
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty() || !"ADMIN".equalsIgnoreCase(userOpt.get().getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bạn không có quyền duyệt phiếu mượn", 403));
        }

        Optional<BookRecord> opt = borrowService.issueRecord(recordId);
        if (opt.isPresent()) {
            BookRecord record = opt.get();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String borrowDate = record.getBorrowDate() != null ? record.getBorrowDate().format(formatter)
                    : LocalDate.now().format(formatter);

            Map<String, Object> data = new HashMap<>();
            data.put("recordId", recordId);
            data.put("borrowDate", borrowDate);
            data.put("status", record.getStatus());

            return ResponseEntity.ok(
                    ApiResponse.success(data,
                            "Xác nhận mượn sách thành công. Sinh viên cần đến nhận sách vào ngày: " + borrowDate));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Không tìm thấy phiếu mượn", 404));
    }

    /**
     * POST /api/borrows/{recordId}/return
     * Cập nhật trả sách (Admin)
     */
    @PostMapping("/{recordId}/return")
    public ResponseEntity<ApiResponse<Map<String, Object>>> returnRecord(
            @PathVariable Integer recordId) {

        String username = SecurityUtil.getAuthenticatedUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập", 401));
        }

        // Kiểm tra role admin (DB lưu là ADMIN viết hoa)
        Optional<User> userOpt = userService.findByUsername(username);
        if (userOpt.isEmpty() || !"ADMIN".equalsIgnoreCase(userOpt.get().getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bạn không có quyền xác nhận trả sách", 403));
        }

        Optional<BookRecord> opt = borrowService.returnRecord(recordId);
        if (opt.isPresent()) {
            Map<String, Object> data = new HashMap<>();
            data.put("recordId", recordId);
            data.put("status", "Đã trả");
            data.put("returnDate", LocalDate.now().toString());

            return ResponseEntity.ok(
                    ApiResponse.success(data, "Cập nhật trạng thái trả sách thành công"));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Không tìm thấy phiếu mượn", 404));
    }
}
