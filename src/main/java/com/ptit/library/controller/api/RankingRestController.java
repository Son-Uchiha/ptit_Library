package com.ptit.library.controller.api;

import com.ptit.library.dto.StudentRankingDto;
import com.ptit.library.dto.response.ApiResponse;
import com.ptit.library.dto.response.PageResponse;
import com.ptit.library.model.Student;
import com.ptit.library.repository.BookRecordRepository;
import com.ptit.library.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ranking")
public class RankingRestController {

    private final StudentRepository studentRepository;
    private final BookRecordRepository bookRecordRepository;

    public RankingRestController(StudentRepository studentRepository,
            BookRecordRepository bookRecordRepository) {
        this.studentRepository = studentRepository;
        this.bookRecordRepository = bookRecordRepository;
    }

    /**
     * GET /api/ranking
     * Lấy bảng xếp hạng sinh viên theo số sách đã mượn
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StudentRankingDto>>> ranking(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        int pageIndex = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageIndex, size);

        Page<Object[]> borrowCounts = bookRecordRepository.findBorrowCounts(pageable);

        Set<String> studentCodes = borrowCounts.getContent().stream()
                .map(r -> String.valueOf(r[0]))
                .collect(Collectors.toSet());

        // Tìm theo studentCode, không phải ID
        Map<String, Student> studentMap = studentRepository.findAllByStudentCodeIn(studentCodes).stream()
                .collect(Collectors.toMap(Student::getStudentCode, Function.identity()));

        List<StudentRankingDto> items = borrowCounts.getContent().stream()
                .map(record -> {
                    String code = String.valueOf(record[0]);
                    Long count = record[1] instanceof Number ? ((Number) record[1]).longValue() : 0L;
                    Student student = studentMap.get(code);
                    return toDto(student, code, count);
                })
                .collect(Collectors.toList());

        PageResponse<StudentRankingDto> pageResponse = PageResponse.<StudentRankingDto>builder()
                .items(items)
                .page(page)
                .size(size)
                .totalPages(borrowCounts.getTotalPages())
                .totalElements(borrowCounts.getTotalElements())
                .hasNext(borrowCounts.hasNext())
                .hasPrevious(borrowCounts.hasPrevious())
                .build();

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    private StudentRankingDto toDto(Student student, String studentCode, Long count) {
        return StudentRankingDto.builder()
                .studentCode(studentCode)
                .fullName(student != null ? student.getFullName() : "")
                .enrollmentYear(resolveEnrollmentYear(student, studentCode))
                .major(student != null ? defaultString(student.getMajor()) : "")
                .borrowCount(count)
                .online(Boolean.FALSE)
                .build();
    }

    private String resolveEnrollmentYear(Student student, String studentCode) {
        if (student != null && student.getEnrollmentYear() != null) {
            return String.valueOf(student.getEnrollmentYear());
        }
        return guessEnrollmentYear(studentCode);
    }

    private String defaultString(String value) {
        return value != null ? value : "";
    }

    private String guessEnrollmentYear(String studentCode) {
        if (studentCode == null)
            return "";
        String digits = studentCode.replaceAll("\\D+", "");
        if (digits.length() >= 2) {
            return "20" + digits.substring(0, 2);
        }
        return "";
    }
}
