package com.example.tools.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageModel {
    private String fromUser;

    private String content;

    private String sessionId;
}
