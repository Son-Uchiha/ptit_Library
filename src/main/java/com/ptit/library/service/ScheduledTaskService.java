package com.ptit.library.service;

import com.ptit.library.model.BookRecord;
import com.ptit.library.repository.BookRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduledTaskService {
    
    @Autowired
    private BookRecordRepository bookRecordRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private BookService bookService;
    
    /**
     * Chạy mỗi ngày lúc 07:00 để kiểm tra và cập nhật các sách quá hạn
     * Gửi thông báo cho những người dùng có sách quá hạn
     */
    @Scheduled(cron = "0 0 7 * * *")
    @Transactional
    public void checkAndNotifyOverdueBooks() {
        System.out.println("[ScheduledTask] Bắt đầu kiểm tra sách quá hạn...");
        
        LocalDate today = LocalDate.now();
        
        // Lấy tất cả bản ghi mượn sách chưa trả
        List<BookRecord> activeRecords = bookRecordRepository.findAll()
            .stream()
            .filter(r -> r.getReturnDate() == null && 
                        r.getDueDate() != null &&
                        ("Đang mượn".equals(r.getStatus()) || "Đang chờ".equals(r.getStatus())))
            .toList();
        
        int overdueCount = 0;
        
        for (BookRecord record : activeRecords) {
            if (today.isAfter(record.getDueDate())) {
                // Nếu chưa phải "Quá hạn", cập nhật status và gửi thông báo
                if (!"Quá hạn".equals(record.getStatus())) {
                    record.setStatus("Quá hạn");
                    bookRecordRepository.save(record);
                    sendOverdueNotification(record);
                    overdueCount++;
                }
            }
        }
        
        System.out.println("[ScheduledTask] Hoàn tất. Cập nhật " + overdueCount + " sách quá hạn.");
    }
    
    private void sendOverdueNotification(BookRecord record) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dueDate = record.getDueDate() != null ? record.getDueDate().format(formatter) : "không xác định";
            long daysOverdue = java.time.temporal.ChronoUnit.DAYS.between(record.getDueDate(), LocalDate.now());
            
            String bookTitle = "";
            if (record.getBookId() != null) {
                Optional<com.ptit.library.model.Book> bookOpt = bookService.findById(record.getBookId());
                if (bookOpt.isPresent()) {
                    bookTitle = bookOpt.get().getTitle();
                }
            }
            
            String content = String.format(
                "Sách '%s' của bạn đã quá hạn trả %d ngày (hạn: %s). " +
                "Vui lòng trả sách sớm để tránh bị phạt.",
                bookTitle, daysOverdue, dueDate
            );
            
            notificationService.createNotification(
                record.getStudentId(),
                content,
                "Sách quá hạn trả"
            );
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi thông báo quá hạn: " + e.getMessage());
        }
    }
}
