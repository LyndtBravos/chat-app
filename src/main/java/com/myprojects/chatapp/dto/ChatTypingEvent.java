package com.myprojects.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatTypingEvent {

    private Long chatId;
    private Long userId;
    private boolean typing;
}