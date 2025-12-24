package com.myprojects.chatapp.websocket;

import com.myprojects.chatapp.dto.PresenceEvent;
import com.myprojects.chatapp.service.ChatService;
import com.myprojects.chatapp.service.MessageService;
import com.myprojects.chatapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class WebSocketPresenceEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final ChatService chatService;
    private final UserService userService;

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;

        Long userId = Long.valueOf(principal.getName());

        // Mark delivered + sync offline messages
        messageService.getAndMarkDelivered(userId);

        chatService.getUserChatIds(userId).forEach(chatId -> messageService.syncOfflineMessages(chatId, userId)
                .forEach(msg ->
                        messagingTemplate.convertAndSendToUser(
                                userId.toString(),
                                "/queue/messages",
                                msg
                        )
                ));

        // Broadcast online presence
        messagingTemplate.convertAndSend(
                "/topic/presence",
                PresenceEvent.builder()
                        .userId(userId)
                        .online(true)
                        .lastSeen(null)
                        .build()
        );
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        Principal principal = event.getUser();
        if (principal == null) return;

        Long userId = Long.valueOf(principal.getName());
        LocalDateTime lastSeen = userService.updateLastSeen(userId);

        messagingTemplate.convertAndSend(
                "/topic/presence",
                PresenceEvent.builder()
                        .userId(userId)
                        .online(false)
                        .lastSeen(lastSeen)
                        .build()
        );
    }
}