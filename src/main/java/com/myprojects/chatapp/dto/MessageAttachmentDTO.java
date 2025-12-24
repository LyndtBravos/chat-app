package com.myprojects.chatapp.dto;

import com.myprojects.chatapp.entity.AttachmentType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageAttachmentDTO {

    private AttachmentType type;
    private String url;
}