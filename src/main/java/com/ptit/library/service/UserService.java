package com.ptit.library.service;

import com.ptit.library.model.Student;
import com.ptit.library.model.User;
import com.ptit.library.model.ValidationResponse;
import com.ptit.library.repository.StudentRepository;
import com.ptit.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findUserWithStudentInfo(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Optional<Student> studentOpt = studentRepository.findByStudentCode(username);
            if (studentOpt.isPresent()) {
                Student student = studentOpt.get();
                user.setFullName(student.getFullName());
                user.setDateOfBirth(student.getDateOfBirth());
                user.setGender(student.getGender());
                user.setPhone(student.getPhone());
                user.setAddress(student.getAddress());
                user.setMajor(student.getMajor());
            }
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public boolean checkUser(String username, String password) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.isPresent() && passwordEncoder.matches(password, user.get().getPassword());
    }

    public boolean checkAccountExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean checkEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public boolean registerUser(User user) {

        if (userRepository.existsByUsername(user.getUsername())) {
            return false;
        }

        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        return true;
    }

    @Transactional
    public void updateAvatar(String username, String avatarPath) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setAvatar(avatarPath);
            userRepository.save(user);
        }
    }

    @Transactional
    public boolean updatePersonalInfo(String username, String fullName, LocalDate dateOfBirth,
            String gender, String phone, String address, String major) {
        Optional<Student> studentOpt = studentRepository.findByStudentCode(username);

        Student student;
        if (studentOpt.isPresent()) {
            student = studentOpt.get();
        } else {
            student = new Student();
            student.setStudentCode(username);
            if (fullName != null && !fullName.trim().isEmpty()) {
                student.setFullName(fullName);
            } else {
                student.setFullName(username);
            }
        }

        if (fullName != null && !fullName.trim().isEmpty()) {
            student.setFullName(fullName);
        }
        if (dateOfBirth != null) {
            student.setDateOfBirth(dateOfBirth);
        }
        if (gender != null && !gender.trim().isEmpty()) {
            student.setGender(gender);
        }
        if (phone != null && !phone.trim().isEmpty()) {
            student.setPhone(phone);
        }
        if (address != null && !address.trim().isEmpty()) {
            student.setAddress(address);
        }
        if (major != null && !major.trim().isEmpty()) {
            student.setMajor(major);
        }

        studentRepository.save(student);
        return true;
    }

    @Transactional
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    public ValidationResponse validateUser(String username, String email,
            String password, String retypePassword) {
        ValidationResponse response = new ValidationResponse();
        response.setValid(true);

        // Validate username
        if (username == null || username.trim().isEmpty()) {
            response.setUsernameError("Tên đăng nhập không được để trống");
            response.setValid(false);
        } else if (username.length() < 3) {
            response.setUsernameError("Tên đăng nhập phải có ít nhất 3 ký tự");
            response.setValid(false);
        }

        // Validate email
        if (email == null || email.trim().isEmpty()) {
            response.setEmailError("Email không được để trống");
            response.setValid(false);
        } else if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            response.setEmailError("Email không hợp lệ");
            response.setValid(false);
        }

        // Validate password
        if (password == null || password.trim().isEmpty()) {
            response.setPasswordError("Mật khẩu không được để trống");
            response.setValid(false);
        } else if (password.length() < 6) {
            response.setPasswordError("Mật khẩu phải có ít nhất 6 ký tự");
            response.setValid(false);
        }

        // Validate retype password
        if (!password.equals(retypePassword)) {
            response.setRetypePasswordError("Mật khẩu nhập lại không khớp");
            response.setValid(false);
        }

        return response;
    }
}
