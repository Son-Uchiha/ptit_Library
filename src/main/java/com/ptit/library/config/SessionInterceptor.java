package com.ptit.library.config;

import com.ptit.library.model.User;
import com.ptit.library.repository.StudentRepository;
import com.ptit.library.service.UserService;
import com.ptit.library.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private StudentRepository studentRepository;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        String username = SecurityUtil.getAuthenticatedUsername();
        if (username != null) {
            // Set session attributes if not already set
            if (session.getAttribute("username") == null) {
                Optional<User> userOpt = userService.findUserWithStudentInfo(username);
                if (userOpt.isEmpty()) {
                    userOpt = userService.findByUsername(username);
                }
                userOpt.ifPresent(user -> {
                    session.setAttribute("username", user.getUsername());
                    session.setAttribute("full_name", user.getFullName());
                    session.setAttribute("email", user.getEmail());
                    session.setAttribute("gender", user.getGender());
                    session.setAttribute("avatar", user.getAvatar());
                    session.setAttribute("role", user.getRole());
                });
            }
        }
        return true;
    }
}