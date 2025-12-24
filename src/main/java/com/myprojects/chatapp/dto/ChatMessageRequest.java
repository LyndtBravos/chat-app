package com.myprojects.chatapp.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageRequest {

    private Long chatId;
    private String content;
    private MessageAttachmentDTO messageAttachment;
}
