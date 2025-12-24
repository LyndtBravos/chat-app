//package com.myprojects.chatapp.websocket;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.myprojects.chatapp.dto.*;
//import com.myprojects.chatapp.entity.AttachmentType;
//import com.myprojects.chatapp.service.ChatService;
//import com.myprojects.chatapp.service.MessageService;
//import com.myprojects.chatapp.service.UserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import org.springframework.web.socket.CloseStatus;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.io.IOException;
//import java.nio.file.AccessDeniedException;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//@Component
//@RequiredArgsConstructor
//public class ChatWebSocketHandler extends TextWebSocketHandler {
//
//    private final MessageService messageService;
//    private final UserService userService;
//    private final ChatService chatService;
//    private final ObjectMapper objectMapper;
//
//    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) {
//
//        Long userId = getUserId(session);
//        if (userId == null) return;
//
//        sessions.put(userId, session);
//
//        broadcastPresence(userId, true);
//
//        messageService.getAndMarkDelivered(userId);
//        syncOfflineMessages(userId, session);
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
//
//        Long userId = getUserId(session);
//        if (userId == null) return;
//
//        sessions.remove(userId);
//
//        LocalDateTime lastSeen = userService.updateLastSeen(userId);
//        broadcastOffline(userId, lastSeen);
//    }
//
//    @Override
//    protected void handleTextMessage(WebSocketSession session, TextMessage message)
//            throws Exception {
//
//        JsonNode payload = objectMapper.readTree(message.getPayload());
//        String type = payload.get("type").asText();
//
//        switch (type) {
//            case "MESSAGE" -> handleChatMessage(session, payload);
//            case "TYPING" -> handleTyping(payload);
//            case "READ" -> handleRead(payload);
//            case "DELIVERED" -> handleDelivered(payload);
//            default -> throw new IllegalArgumentException("Unknown WS type: " + type);
//        }
//    }
//
//    private void handleChatMessage(WebSocketSession session, JsonNode payload)
//            throws AccessDeniedException {
//
//        Long senderId = getUserId(session);
//
//        MessageDTO message = messageService.sendMessage(buildSendRequest(payload),senderId);
//
//        broadcastToChat(message);
//    }
//
//    private void handleTyping(JsonNode payload) {
//
//        ChatTypingEvent event = new ChatTypingEvent(
//                payload.get("chatId").asLong(),
//                payload.get("userId").asLong(),
//                payload.get("typing").asBoolean()
//        );
//
//        chatService.getParticipants(event.getChatId()).stream()
//                .filter(u -> !u.getId().equals(event.getUserId()))
//                .forEach(u -> send(sessions.get(u.getId()), event));
//    }
//
//    private void handleDelivered(JsonNode payload) {
//
//        messageService.getAndMarkDelivered(payload.get("messageId").asLong());
//
////        notifySender(updated);
//    }
//
//    private void handleRead(JsonNode payload) throws AccessDeniedException {
//
//        List<MessageDTO> readMessages =
//                messageService.markRead(payload.get("chatId").asLong(), payload.get("readerId").asLong());
//        readMessages.forEach(this::notifySender);
//    }
//
//    private SendMessageRequest buildSendRequest(JsonNode payload) {
//
//        SendMessageRequest request = SendMessageRequest.builder().build();
//        request.setChatId(payload.get("chatId").asLong());
//        request.setContent(payload.get("content").asText());
//
//        if (payload.has("attachment")) {
//            JsonNode att = payload.get("attachment");
//
//            MessageAttachmentDTO attachment = MessageAttachmentDTO.builder().build();
//            attachment.setType(AttachmentType.valueOf(att.get("type").asText().toUpperCase()));
//            attachment.setUrl(att.get("url").asText());
//            request.setAttachment(attachment);
//        }
//
//        return request;
//    }
//
//    private void broadcastToChat(MessageDTO message) {
//
//        chatService.getParticipants(message.getChatId())
//                .forEach(user -> send(
//                        sessions.get(user.getId()),
//                        message
//                ));
//    }
//
//    private void notifySender(MessageDTO message) {
//        send(sessions.get(message.getSenderId()), message);
//    }
//
//    private void syncOfflineMessages(Long userId, WebSocketSession session) {
//
//        chatService.getUserChatIds(userId).forEach(chatId -> {
//            List<MessageDTO> missed =
//                    messageService.syncOfflineMessages(chatId, userId);
//
//            missed.forEach(msg -> {
//                send(session, msg);
//                notifySenderDelivered(msg.getSenderId(), msg.getId());
//            });
//        });
//    }
//
//    private void notifySenderDelivered(Long senderId, Long messageId) {
//
//        WebSocketSession senderSession = sessions.get(senderId);
//        if (senderSession == null) return;
//
//        send(senderSession, Map.of(
//                "type", "DELIVERED",
//                "messageId", messageId
//        ));
//    }
//
//    private void broadcastPresence(Long userId, boolean online) {
//
//        PresenceEvent event = PresenceEvent.builder()
//                .userId(userId)
//                .online(online)
//                .lastSeen(null)
//                .build();
//
//        sessions.values().forEach(s -> send(s, event));
//    }
//
//    private void broadcastOffline(Long userId, LocalDateTime lastSeen) {
//
//        PresenceEvent event = PresenceEvent.builder()
//                .userId(userId)
//                .online(false)
//                .lastSeen(lastSeen)
//                .build();
//
//        sessions.values().forEach(s -> send(s, event));
//    }
//
//    private void send(WebSocketSession session, Object payload) {
//        if (session == null || !session.isOpen()) return;
//
//        try {
//            session.sendMessage(
//                    new TextMessage(objectMapper.writeValueAsString(payload))
//            );
//        } catch (IOException ignored) {}
//    }
//
//    private Long getUserId(WebSocketSession session) {
//        return (Long) session.getAttributes().get("userId");
//    }
//}