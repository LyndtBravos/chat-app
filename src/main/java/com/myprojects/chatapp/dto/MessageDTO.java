package com.myprojects.chatapp.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class MessageDTO {

    private Long id;

    private Long chatId;

    private Long senderId;

    private String senderUsername;

    private String content;

    private LocalDateTime timestamp;

    private boolean read;

    private String attachmentType;

    private String attachmentUrl;
}