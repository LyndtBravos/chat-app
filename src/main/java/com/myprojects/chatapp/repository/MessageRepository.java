package com.myprojects.chatapp.repository;

import com.myprojects.chatapp.entity.Chat;
import com.myprojects.chatapp.entity.Message;
import com.myprojects.chatapp.entity.MessageStatus;
import com.myprojects.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatIdAndStatusAndSenderIdNot(Long chatId,MessageStatus status,Long senderId);
    List<Message> findByChatIdAndStatus(Long chatId, MessageStatus status);
    List<Message> findBySenderIdAndStatus(Long senderId, MessageStatus status);
    List<Message> findByChat(Chat chat);
    List<Message> findByChatIdAndSenderNotAndStatus(Long chatId, User reader, MessageStatus status);

    @Query("""
    SELECT m FROM Message m
    JOIN m.chat c
    JOIN c.participants p
    WHERE p.id = :recipientId
      AND m.sender.id <> :recipientId
      AND m.status = com.myprojects.chatapp.entity.MessageStatus.SENT
""")
    List<Message> findUndeliveredMessagesForUser(@Param("recipientId") Long recipientId);

    void deleteAllByChat(Chat chat);

    List<Message> findByChatAndStatus(Chat chat, MessageStatus messageStatus);
}