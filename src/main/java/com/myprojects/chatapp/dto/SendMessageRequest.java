package com.myprojects.chatapp.dto;

import lombok.*;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    private Long chatId;
    private String content;
    private MessageAttachmentDTO attachment;
}