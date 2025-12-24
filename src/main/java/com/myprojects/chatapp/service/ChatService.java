package com.myprojects.chatapp.service;

import com.myprojects.chatapp.dto.ChatDTO;
import com.myprojects.chatapp.entity.Chat;
import com.myprojects.chatapp.entity.ChatParticipant;
import com.myprojects.chatapp.entity.ChatType;
import com.myprojects.chatapp.entity.User;
import com.myprojects.chatapp.repository.ChatParticipantRepository;
import com.myprojects.chatapp.repository.ChatRepository;
import com.myprojects.chatapp.repository.MessageRepository;
import com.myprojects.chatapp.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<ChatDTO> getUserChats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return participantRepository.findByUser(user)
                .stream()
                .map(ChatParticipant::getChat)
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public ChatDTO createChat(String name, boolean isGroup, List<Long> participantIds, Long creatorId) {

        if (!participantIds.contains(creatorId))
            participantIds.add(creatorId);

        List<User> users = userRepository.findAllById(participantIds);
        if (users.size() != participantIds.size())
            throw new EntityNotFoundException("One or more users not found");

        Chat chat = Chat.builder()
                .name(name)
                .isGroup(isGroup)
                .type(isGroup ? ChatType.GROUP : ChatType.PRIVATE)
                .createdAt(LocalDateTime.now())
                .build();

        chatRepository.save(chat);

        users.forEach(user ->
                participantRepository.save(
                        ChatParticipant.builder()
                                .chat(chat)
                                .user(user)
                                .build()
                )
        );

        return convertToDTO(chat);
    }

    @Transactional(readOnly = true)
    public List<User> getParticipants(Long chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        return participantRepository.findByChat(chat)
                .stream()
                .map(ChatParticipant::getUser)
                .toList();
    }

    @Transactional
    public ChatDTO updateChat(Long chatId, ChatDTO dto) {

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        chat.setName(dto.getName());
        chat.setGroup(dto.isGroup());

        if (dto.getParticipantIds() != null) {
            participantRepository.deleteAllByChat(chat);

            dto.getParticipantIds().forEach(uid -> {
                User user = userRepository.findById(uid)
                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

                participantRepository.save(
                        ChatParticipant.builder()
                                .chat(chat)
                                .user(user)
                                .build()
                );
            });
        }

        return convertToDTO(chatRepository.save(chat));
    }

    @Transactional
    public String deleteChat(Long chatId, String username) throws AccessDeniedException {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        boolean isParticipant = chat.getParticipants()
                .stream()
                .anyMatch(a -> a.getUser().getUsername().equalsIgnoreCase(username));

        if (!isParticipant)
            throw new AccessDeniedException("User is not a participant in this chat");

        chatRepository.delete(chat);
        return "Chat deletion was a success!";
    }

    public List<Long> getUserChatIds(Long userId) {
        return participantRepository.findByUserId(userId)
                .stream()
                .map(cp -> cp.getChat().getId())
                .toList();
    }

    public ChatDTO convertToDTO(Chat chat) {
        List<Long> participantIds =
                participantRepository.findByChat(chat)
                        .stream()
                        .map(cp -> cp.getUser().getId())
                        .toList();

        return ChatDTO.builder()
                .id(chat.getId())
                .name(chat.getName())
                .isGroup(chat.isGroup())
                .participantIds(participantIds)
                .build();
    }
}