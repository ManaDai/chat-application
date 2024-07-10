package com.example.one_on_one_chat_app_websocket.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.one_on_one_chat_app_websocket.domain.ChatMessage;
import com.example.one_on_one_chat_app_websocket.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository repository;
    private final ChatRoomService chatRoomService;

    public ChatMessage save(ChatMessage chatMessage) {
        var chatId = chatRoomService
                .getChatRoomId(chatMessage.getSenderId(), chatMessage.getRecipientId(), true)
                .orElseThrow(); 
        chatMessage.setChatId(chatId);
        repository.save(chatMessage);
        return chatMessage;
    }

    public List<ChatMessage> findChatMessages(String senderId, String recipientId) {
        var chatId = chatRoomService.getChatRoomId(senderId, recipientId, false);
        return chatId.map(repository::findByChatId).orElse(new ArrayList<>());
    }

    public Optional<ChatMessage> findById(String id) {
        Optional<ChatMessage> chatMessageOptional = repository.findById(id);
        return chatMessageOptional;
    }

    @Transactional
    public ChatMessage delete(ChatMessage chatMessage) {
        repository.delete(chatMessage);
        return chatMessage;
    }

}