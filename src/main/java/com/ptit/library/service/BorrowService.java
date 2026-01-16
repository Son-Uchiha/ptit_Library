package com.ptit.library.service;

import com.ptit.library.model.Book;
import com.ptit.library.model.BookRecord;
import com.ptit.library.model.Notification;
import com.ptit.library.model.Student;
import com.ptit.library.model.User;
import com.ptit.library.repository.BookRecordRepository;
import com.ptit.library.repository.BookRepository;
import com.ptit.library.repository.StudentRepository;
import com.ptit.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BorrowService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookRecordRepository bookRecordRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public List<Integer> borrowBooks(String username, int[] bookIds) {
        LocalDate today = LocalDate.now();
        List<Integer> failedBooks = new ArrayList<>();

        // Đảm bảo student tồn tại trong bảng Students (do có Foreign Key constraint)
        ensureStudentExists(username);

        for (int bookId : bookIds) {
            Optional<Book> bookOpt = bookRepository.findById(bookId);

            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();

                // Check if book is available
                if (book.getCopiesAvailable() > 0) {
                    // Update book availability
                    book.setCopiesAvailable(book.getCopiesAvailable() - 1);
                    bookRepository.save(book);
                    // Create borrow record
                    BookRecord record = new BookRecord();
                    record.setStudentId(username);
                    record.setBookId(bookId);
                    record.setBorrowDate(today);
                    record.setDueDate(today.plusDays(14));
                    record.setStatus("Đang chờ");
                    bookRecordRepository.save(record);
                } else {
                    failedBooks.add(bookId);
                }
            } else {
                failedBooks.add(bookId);
            }
        }

        return failedBooks;
    }

    /**
     * Đảm bảo student tồn tại trong bảng Students
     * Nếu chưa có thì tự động tạo từ thông tin User
     */
    private void ensureStudentExists(String username) {
        if (!studentRepository.existsByStudentCode(username)) {
            // Lấy thông tin từ User nếu có
            Optional<User> userOpt = userRepository.findByUsername(username);

            Student student = new Student();
            student.setStudentCode(username);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                student.setFullName(user.getName() != null ? user.getName() : username);
                student.setEmail(user.getEmail());
            } else {
                student.setFullName(username);
            }

            studentRepository.save(student);
        }
    }

    public List<BookRecord> getRecordsByStudentId(String studentId) {
        return bookRecordRepository.findByStudentId(studentId);
    }

    public List<Object[]> getRecordsWithBookInfo(String studentId) {
        return bookRecordRepository.findRecordsWithBookInfo(studentId);
    }

    public List<Object[]> getAllRecordsWithBookInfo(String status) {
        if (status != null && !status.isBlank()) {
            return bookRecordRepository.findAllRecordsWithBookInfoByStatus(status);
        }
        return bookRecordRepository.findAllRecordsWithBookInfo();
    }

    @Transactional
    public Optional<BookRecord> issueRecord(Integer recordId) {
        Optional<BookRecord> opt = bookRecordRepository.findById(recordId);
        if (opt.isPresent()) {
            BookRecord r = opt.get();
            if (r.getStatus() == null || r.getStatus().equals("Đang chờ")) {
                r.setStatus("Đang mượn");
                r.setBorrowDate(LocalDate.now());
                bookRecordRepository.save(r);

                sendBorrowConfirmationNotification(r);
            }
            return Optional.of(r);
        }
        return Optional.empty();
    }

    private void sendBorrowConfirmationNotification(BookRecord record) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate borrowDate = record.getBorrowDate();
            String borrowDateStr = borrowDate != null ? borrowDate.format(formatter)
                    : LocalDate.now().format(formatter);

            String bookTitle = "";
            if (record.getBookId() != null) {
                Optional<Book> bookOpt = bookRepository.findById(record.getBookId());
                if (bookOpt.isPresent()) {
                    bookTitle = bookOpt.get().getTitle();
                }
            }

            String content = String.format(
                    "Đơn mượn sách '%s' của bạn đã được xác nhận. " +
                            "Vui lòng đến thư viện để nhận sách vào ngày %s.",
                    bookTitle, borrowDateStr);

            // Lưu notification vào DB
            Notification notification = notificationService.createNotification(
                    record.getStudentId(),
                    content,
                    "Xác nhận mượn sách");

            // Push realtime notification qua WebSocket
            sendRealtimeNotification(record.getStudentId(), notification);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Push notification realtime qua WebSocket
     */
    private void sendRealtimeNotification(String userId, Notification notification) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", notification.getId());
            payload.put("title", notification.getTitle());
            payload.put("content", notification.getContent());
            payload.put("type", notification.getNotificationType());
            payload.put("createdAt", notification.getCreatedAt().toString());
            payload.put("isRead", false);

            // Gửi tới topic cá nhân của user (FE subscribe topic này)
            messagingTemplate.convertAndSend("/topic/notifications/" + userId, payload);

            // Cũng gửi tới user queue (nếu user có session)
            messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/notifications",
                    payload);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public Optional<BookRecord> returnRecord(Integer recordId) {
        Optional<BookRecord> opt = bookRecordRepository.findById(recordId);
        if (opt.isPresent()) {
            BookRecord r = opt.get();
            r.setStatus("Đã trả");
            r.setReturnDate(LocalDate.now());
            bookRecordRepository.save(r);

            // increment book copy available
            if (r.getBookId() != null) {
                Optional<Book> bookOpt = bookRepository.findById(r.getBookId());
                bookOpt.ifPresent(book -> {
                    book.setCopiesAvailable(book.getCopiesAvailable() + 1);
                    bookRepository.save(book);
                });
            }

            return Optional.of(r);
        }
        return Optional.empty();
    }
}
