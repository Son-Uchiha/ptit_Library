package com.ptit.library.service;

import com.ptit.library.model.User;
import com.ptit.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Random;

@Service
public class PasswordResetService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Value("${app.password.reset.length:12}")
    private int tempPasswordLength;
    
    @Transactional
    public boolean processForgotPassword(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        if (userOpt.isEmpty()) {
            logger.warn("Không tìm thấy user: {}", username);
            return false;
        }
        
        User user = userOpt.get();
        
        String tempPassword = generateTempPassword();
        
        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);
        
        emailService.sendPasswordResetEmail(
            user.getEmail(),
            username,
            tempPassword
        );
        
        logger.info("Reset mật khẩu thành công cho user: {}", username);
        return true;
    }
    
    private String generateTempPassword() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < tempPasswordLength; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }
        
        return password.toString();
    }
}
