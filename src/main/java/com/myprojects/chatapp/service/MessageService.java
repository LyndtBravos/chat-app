package com.myprojects.chatapp.service;

import com.myprojects.chatapp.dto.MessageDTO;
import com.myprojects.chatapp.dto.SendMessageRequest;
import com.myprojects.chatapp.entity.*;
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
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageDTO sendMessage(SendMessageRequest request, Long senderId) throws AccessDeniedException {

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Chat chat = chatRepository.findById(request.getChatId())
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        if (!chat.getParticipants().contains(sender))
            throw new AccessDeniedException("User not part of this chat");

        Message message = Message.builder()
                .chat(chat)
                .sender(sender)
                .content(request.getContent())
                .status(MessageStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();

        if (request.getAttachment() != null &&
                request.getAttachment().getType() != AttachmentType.NONE) {

            message.setAttachment(
                    MessageAttachment.builder()
                            .type(request.getAttachment().getType())
                            .url(request.getAttachment().getUrl())
                            .build()
            );
        }

        return convertToDTO(messageRepository.save(message));
    }

    @Transactional
    public void getAndMarkDelivered(Long recipientId) {

        List<Message> messages =
                messageRepository.findUndeliveredMessagesForUser(recipientId);

        messages.stream()
                .filter(msg -> !msg.getSender().getId().equals(recipientId))
                .forEach(msg -> msg.setStatus(MessageStatus.DELIVERED));

        messageRepository.saveAll(messages);
    }

    @Transactional
    public List<MessageDTO> syncOfflineMessages(Long chatId, Long userId) {
        List<Message> messages = messageRepository
                .findByChatIdAndStatusAndSenderIdNot(chatId, MessageStatus.SENT, userId);
        messages.forEach(msg -> msg.setStatus(MessageStatus.DELIVERED));
        return messageRepository.saveAll(messages).stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public List<MessageDTO> markRead(Long chatId, Long readerId) throws AccessDeniedException {

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        User reader = userRepository.findById(readerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!chat.getParticipants().contains(reader))
            throw new AccessDeniedException("User not part of this chat");

        List<Message> messages =
                messageRepository.findByChatAndStatus(chat, MessageStatus.DELIVERED);

        messages = messages.stream()
                        .filter(msg -> !msg.getSender().equals(reader))
                        .peek(msg -> msg.setStatus(MessageStatus.READ))
                        .toList();

        return messageRepository.saveAll(messages)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getMessagesByChat(Long chatId, Long userId) throws AccessDeniedException {

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        if (chat.getParticipants()
                .stream()
                .noneMatch(u -> u.getId().equals(userId)))
            throw new AccessDeniedException("Not allowed to view this chat");


        return messageRepository.findByChat(chat)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    private MessageDTO convertToDTO(Message message) {

        MessageAttachment att = message.getAttachment();

        return MessageDTO.builder()
                .id(message.getId())
                .chatId(message.getChat().getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .content(message.getContent())
                .timestamp(message.getCreatedAt())
                .read(message.getStatus() == MessageStatus.READ)
                .attachmentType(att != null ? att.getType().name() : "NONE")
                .attachmentUrl(att != null ? att.getUrl() : "")
                .build();
    }

    public MessageDTO updateMessage(Long id, SendMessageRequest smr) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        message.setContent(smr.getContent());

        return convertToDTO(messageRepository.save(message));
    }

    public String deleteMessage(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));

        messageRepository.delete(message);
        return "Message deletion was a success";
    }
}