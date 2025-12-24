package com.myprojects.chatapp.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatDTO {

    private Long id;

    private String name;

    private boolean isGroup;

    private List<Long> participantIds;
}