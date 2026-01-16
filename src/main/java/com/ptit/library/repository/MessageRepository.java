package com.ptit.library.repository;

import com.ptit.library.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

       @Query("SELECT m FROM Message m WHERE " +
                     "(m.senderId = :userId OR m.receiverId = :userId) " +
                     "ORDER BY m.createdAt DESC")
       List<Message> findMessagesByUserId(@Param("userId") String userId);

       @Query("""
                         SELECT m FROM Message m
                         WHERE (m.senderId = :u1 AND m.receiverId = :u2)
                            OR (m.senderId = :u2 AND m.receiverId = :u1)
                         ORDER BY m.createdAt ASC
                     """)
       List<Message> findConversation(@Param("u1") String u1, @Param("u2") String u2);

       List<Message> findByReceiverIdAndIsReadFalse(String receiverId);
}