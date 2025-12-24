package com.myprojects.chatapp.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PresenceEvent {

    private Long userId;
    private boolean online;
    private LocalDateTime lastSeen;
}