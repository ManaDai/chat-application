package com.example.one_on_one_chat_app_websocket.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.example.one_on_one_chat_app_websocket.domain.ChatRoom;

@DataMongoTest
public class ChatRoomRepositoryTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ChatRoomRepository chatRoomRepository;


    @BeforeEach
    public void setUp(){
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setSenderId("sender1");
        chatRoom.setRecipientId("recipient1");
        mongoTemplate.save(chatRoom);
    }


    @Test
    public void testFindBySenderIdAndRecipientId() {
        String senderId = "sender1";
        String recipientId = "recipient1";

        Optional<ChatRoom> optionalChatRoom = chatRoomRepository.findBySenderIdAndRecipientId(senderId, recipientId);

        assertTrue(optionalChatRoom.isPresent(), "ChatRoom should be present");
        ChatRoom chatRoom = optionalChatRoom.get();
        assertEquals(senderId, chatRoom.getSenderId(), "Sender IDs should match");
        assertEquals(recipientId, chatRoom.getRecipientId(), "Recipient IDs should match");
    }
}
