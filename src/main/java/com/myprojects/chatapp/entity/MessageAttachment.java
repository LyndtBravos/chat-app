package com.myprojects.chatapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageAttachment {
    @Id
    @GeneratedValue
    Long id;

    String url;

    @Enumerated(EnumType.STRING)
    AttachmentType type;
}