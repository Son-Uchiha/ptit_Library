package com.ptit.library.repository;

import com.ptit.library.model.BookRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRecordRepository extends JpaRepository<BookRecord, Integer> {

        List<BookRecord> findByStudentId(String studentId);

        List<BookRecord> findByStudentIdAndStatus(String studentId, String status);

        @Query(value = "SELECT r.student_code, r.id as record_id, b.id as book_id, b.title, " +
                        "b.author, b.published_year, r.borrow_date, r.status " +
                        "FROM Books b JOIN BorrowRecords r ON b.id = r.book_id " +
                        "WHERE r.student_code = :studentId", nativeQuery = true)
        List<Object[]> findRecordsWithBookInfo(@Param("studentId") String studentId);

        @Query(value = "SELECT r.student_code, r.id as record_id, b.id as book_id, b.title, " +
                        "b.author, b.published_year, r.borrow_date, r.status " +
                        "FROM Books b JOIN BorrowRecords r ON b.id = r.book_id " +
                        "ORDER BY r.id DESC", nativeQuery = true)
        List<Object[]> findAllRecordsWithBookInfo();

        @Query(value = "SELECT r.student_code, r.id as record_id, b.id as book_id, b.title, " +
                        "b.author, b.published_year, r.borrow_date, r.status " +
                        "FROM Books b JOIN BorrowRecords r ON b.id = r.book_id " +
                        "WHERE r.status = :status " +
                        "ORDER BY r.id DESC", nativeQuery = true)
        List<Object[]> findAllRecordsWithBookInfoByStatus(@Param("status") String status);

        @Query(value = "SELECT br.student_code AS student_code, COUNT(*) AS borrow_count " +
                        "FROM BorrowRecords br GROUP BY br.student_code ORDER BY borrow_count DESC", countQuery = "SELECT COUNT(DISTINCT br.student_code) FROM BorrowRecords br", nativeQuery = true)
        Page<Object[]> findBorrowCounts(Pageable pageable);
}
