package com.ptit.library.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "BorrowRecords")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "student_code", length = 50)
    private String studentId;

    @Column(name = "book_id")
    private Integer bookId;

    @Column(name = "borrow_date")
    private LocalDate borrowDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "fine_amount")
    private java.math.BigDecimal fineAmount;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Transient
    private String title; // From Books table join

    @Transient
    private String author; // From Books table join

    @Transient
    private Integer publishedYear; // From Books table join

    public BookRecord(String studentId, Integer id, Integer bookId, String title,
            String author, Integer publishedYear, String borrowDate, String status) {
        this.studentId = studentId;
        this.id = id;
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.publishedYear = publishedYear;
        this.status = status;
    }
}
