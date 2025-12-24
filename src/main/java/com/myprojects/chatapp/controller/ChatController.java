package com.myprojects.chatapp.controller;

import com.myprojects.chatapp.dto.ChatDTO;
import com.myprojects.chatapp.security.CustomUserDetails;
import com.myprojects.chatapp.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chats")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ChatDTO create(@RequestBody ChatDTO dto) {
        List<Long> participantsIDs = dto.getParticipantIds();

        CustomUserDetails user = (CustomUserDetails) Objects.requireNonNull(
                SecurityContextHolder.getContext().getAuthentication()
        ).getPrincipal();

        Long userId = user != null && user.getId() != null ? user.getId() : 0L;
        return chatService.createChat(dto.getName(), dto.isGroup(), participantsIDs, userId);
    }

    @PutMapping("/{id}")
    public ChatDTO update(@PathVariable Long id, @RequestBody ChatDTO dto) {
        return chatService.updateChat(id, dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id, Authentication auth)
            throws AccessDeniedException {
        return ResponseEntity.ok(chatService.deleteChat(id, auth.getName()));
    }

    @GetMapping("/user/{userId}")
    public List<ChatDTO> getUserChats(@PathVariable Long userId) {
        return chatService.getUserChats(userId);
    }
}