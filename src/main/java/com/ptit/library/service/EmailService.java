package com.ptit.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    private static final String EMAIL_SUBJECT = "PTIT Library - Đặt lại mật khẩu";
    
    @Async
    public void sendPasswordResetEmail(String recipientEmail, String username, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(recipientEmail);
            helper.setSubject(EMAIL_SUBJECT);
            helper.setText(buildPasswordResetEmailBody(username, tempPassword), true);
            
            mailSender.send(message);
            logger.info("Email đặt lại mật khẩu đã gửi tới: {}", recipientEmail);
            
        } catch (MessagingException e) {
            logger.error("Lỗi khi gửi email tới {}: {}", recipientEmail, e.getMessage());
        } catch (Exception e) {
            logger.error("Lỗi khi gửi email tới {}: {}", recipientEmail, e.getMessage());
        }
    }
    
    private String buildPasswordResetEmailBody(String username, String tempPassword) {
        return "<html><body style=\"font-family: Arial, sans-serif; font-size: 14px;\">" +
               "<p>Xin chào <strong>" + username + "</strong>,</p>" +
               "<p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản PTIT Library.</p>" +
               "<p><strong>Mật khẩu tạm thời của bạn là:</strong> <code style=\"background-color: #f0f0f0; padding: 5px;\">" + tempPassword + "</code></p>" +
               "<p><strong>Vui lòng:</strong></p>" +
               "<ol>" +
               "<li>Đăng nhập bằng mật khẩu tạm thời trên</li>" +
               "<li>Truy cập 'Hồ sơ' để đổi mật khẩu mới</li>" +
               "</ol>" +
               "<p style=\"color: #999;\">Nếu bạn không yêu cầu điều này, vui lòng bỏ qua email này.</p>" +
               "<p style=\"color: #999;\">Trân trọng,<br/>PTIT Library System</p>" +
               "</body></html>";
    }
}
