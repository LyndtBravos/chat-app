package com.myprojects.chatapp.controller;

import com.myprojects.chatapp.dto.MessageDTO;
import com.myprojects.chatapp.dto.SendMessageRequest;
import com.myprojects.chatapp.security.CustomUserDetails;
import com.myprojects.chatapp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/chat/{chatId}")
    public List<MessageDTO> getMessages(@PathVariable Long chatId, Authentication auth)
            throws AccessDeniedException {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        Long userId = user != null && user.getId() != null ? user.getId() : 0L;

        return messageService.getMessagesByChat(chatId, userId);
    }

    @PostMapping
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestBody SendMessageRequest request,
            Authentication authentication
    ) throws AccessDeniedException {
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Long userId = user != null && user.getId() != null ? user.getId() : 0L;

        return ResponseEntity.ok(messageService.sendMessage(request, userId));
    }

    @PostMapping("/read/{chatId}")
    public void readChats(@PathVariable Long chatId, Authentication auth) throws AccessDeniedException {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        Long userId = user != null && user.getId() != null ? user.getId() : 0L;

        messageService.markRead(chatId, userId);
    }

    @PutMapping("/update/{id}")
    public MessageDTO updateMessage(@PathVariable Long id, @RequestBody SendMessageRequest messageDTO){
        return messageService.updateMessage(id, messageDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.deleteMessage(id));
    }
}