package com.myprojects.chatapp.repository;

import com.myprojects.chatapp.entity.Chat;
import com.myprojects.chatapp.entity.ChatParticipant;
import com.myprojects.chatapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    // Find all participants for a given user
    List<ChatParticipant> findByUser(User user);

    // Find all participants for a given chat
    List<ChatParticipant> findByChat(Chat chat);

    // Delete all participants for a given chat
    void deleteAllByChat(Chat chat);

    List<ChatParticipant> findByUserId(Long userId);
}
