package com.myprojects.chatapp.controller;

import com.myprojects.chatapp.dto.ChatTypingEvent;
import com.myprojects.chatapp.dto.MessageDTO;
import com.myprojects.chatapp.dto.SendMessageRequest;
import com.myprojects.chatapp.security.CustomUserDetails;
import com.myprojects.chatapp.service.ChatService;
import com.myprojects.chatapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.nio.file.AccessDeniedException;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final MessageService messageService;
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void send(SendMessageRequest request, Authentication auth)
            throws AccessDeniedException {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        Long senderId = user != null && user.getId() != null ? user.getId() : 0L;

        MessageDTO saved =
                messageService.sendMessage(request, senderId);

        chatService.getParticipants(saved.getChatId())
                .forEach(i ->
                        messagingTemplate.convertAndSendToUser(
                                i.getId().toString(),
                                "/queue/messages",
                                i
                        )
                );
    }

    @MessageMapping("/chat.typing")
    public void typing(ChatTypingEvent event) {

        chatService.getParticipants(event.getChatId()).stream()
                .filter(u -> !u.getId().equals(event.getUserId()))
                .forEach(u ->
                        messagingTemplate.convertAndSendToUser(
                                u.getId().toString(),
                                "/queue/typing",
                                event
                        )
                );
    }
}