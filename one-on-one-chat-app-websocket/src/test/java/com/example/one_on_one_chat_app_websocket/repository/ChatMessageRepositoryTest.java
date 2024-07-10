package com.example.one_on_one_chat_app_websocket.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import com.example.one_on_one_chat_app_websocket.domain.ChatMessage;
import org.springframework.data.mongodb.core.MongoTemplate;

@DataMongoTest
// @ComponentScan("com.example.one_on_one_chat_app_websocket")
public class ChatMessageRepositoryTest {

    
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @BeforeEach
    public void setUp() {
        ChatMessage message = new ChatMessage();
        message.setChatId("testChatId");
        message.setContent("This is a test message.");
        mongoTemplate.save(message);
    }

    @Test
    public void testFindByChatId() {
        List<ChatMessage> messages = chatMessageRepository.findByChatId("testChatId");
        assertFalse(messages.isEmpty(), "Message list should not be empty");
        assertEquals("This is a test message.", messages.get(0).getContent());
    }
}
