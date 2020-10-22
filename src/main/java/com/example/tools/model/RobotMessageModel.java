package com.example.tools.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RobotMessageModel extends MessageModel {
    private String commandName;

    private Long toId;

    private String accessToken;
}
