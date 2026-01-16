package com.ptit.library.repository;

import com.ptit.library.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByStudentCode(String studentCode);

    boolean existsByStudentCode(String studentCode);

    List<Student> findAllByStudentCodeIn(Collection<String> studentCodes);
}
