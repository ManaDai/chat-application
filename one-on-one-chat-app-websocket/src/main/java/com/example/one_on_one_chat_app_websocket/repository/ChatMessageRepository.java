package com.example.one_on_one_chat_app_websocket.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.one_on_one_chat_app_websocket.domain.ChatMessage;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    List<ChatMessage> findByChatId(String chatId);


}