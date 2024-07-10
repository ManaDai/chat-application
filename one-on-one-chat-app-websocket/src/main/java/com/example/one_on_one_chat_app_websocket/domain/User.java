package com.example.one_on_one_chat_app_websocket.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document
public class User {
    @Id
    private String nickName;
    private String fullName;
    private Status status;
}