package com.ptit.library.service;

import com.ptit.library.model.Message;
import com.ptit.library.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Transactional
    public Message sendMessage(String senderId, String receiverId, String content) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsRead(false);
        message.setCreatedAt(LocalDateTime.now());
        return messageRepository.save(message);
    }

    public List<Message> getMessagesByUserId(String userId) {
        return messageRepository.findMessagesByUserId(userId);
    }

    public List<Message> getConversation(String user1, String user2) {
        return messageRepository.findConversation(user1, user2);
    }

    public List<Message> getUnreadMessages(String receiverId) {
        return messageRepository.findByReceiverIdAndIsReadFalse(receiverId);
    }

    @Transactional
    public void markAsRead(Integer messageId) {
        messageRepository.findById(messageId).ifPresent(message -> {
            message.setIsRead(true);
            messageRepository.save(message);
        });
    }
}