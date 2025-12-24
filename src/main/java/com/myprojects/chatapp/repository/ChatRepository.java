package com.myprojects.chatapp.repository;

import com.myprojects.chatapp.dto.MessageDTO;
import com.myprojects.chatapp.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByParticipants_Id(Long userId);
    List<MessageDTO> getMessagesById(Long chatID);

}