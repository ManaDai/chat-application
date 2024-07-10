package com.example.one_on_one_chat_app_websocket.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.example.one_on_one_chat_app_websocket.domain.ChatMessage;
import com.example.one_on_one_chat_app_websocket.domain.ChatNotification;
import com.example.one_on_one_chat_app_websocket.service.ChatMessageService;


@SpringBootTest
@AutoConfigureMockMvc
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatMessageService chatMessageService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ChatController chatController;

    @BeforeEach
    public void setup() {
        chatMessageService = Mockito.mock(ChatMessageService.class);
        chatController = new ChatController(chatMessageService, messagingTemplate);

        // saveメソッドのモック動作を設定
        when(chatMessageService.save(any(ChatMessage.class))).thenReturn(new ChatMessage());

        // deleteメソッドのモック動作を設定
        when(chatMessageService.delete(any(ChatMessage.class))).thenReturn(new ChatMessage());
    }

    @Test
    public void testDeleteMessage() throws Exception {
        String messageId = "abc123";
        ChatMessage mockMessage = new ChatMessage();
        mockMessage.setId(messageId);

        // Mock serviceの設定
        when(chatMessageService.findById(eq(messageId))).thenReturn(Optional.of(mockMessage));

        // DELETEリクエストの送信
        mockMvc.perform(MockMvcRequestBuilders.delete("/messages/{messageId}", messageId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Message deleted successfully"));

        // メッセージが削除されたことを確認するための検証
        verify(chatMessageService, times(1)).delete(eq(mockMessage));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/messageDeleted"), eq(messageId));
    }

    @Test
    public void testProcessMessage() {
        // テスト用のChatMessageオブジェクトを作成
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId("sender1");
        chatMessage.setRecipientId("recipient1");
        chatMessage.setContent("Hello!");

        // saveメソッドのモック動作を設定
        when(chatMessageService.save(any())).thenReturn(chatMessage);

        // テスト対象のメソッドを実行
        chatController.processMessage(chatMessage);

        // convertAndSendToUserが1回呼ばれたことを検証
        verify(messagingTemplate, times(1)).convertAndSendToUser(
                eq(chatMessage.getRecipientId()), eq("/queue/messages"),
                any(ChatNotification.class));
    }

    @Test
    public void testFindChatMessages() {
        // テスト用のパスパラメータを設定
        String senderId = "sender1";
        String recipientId = "recipient1";
        String chatId = "chatId";

        // findChatMessagesメソッドのモック動作を設定
        List<ChatMessage> mockMessages = new ArrayList<>();
        mockMessages.add(new ChatMessage("1", chatId, senderId, recipientId, "Message 1", new Date()));
        mockMessages.add(new ChatMessage("2", chatId, senderId, recipientId, "Message 2", new Date()));
        when(chatMessageService.findChatMessages(senderId, recipientId)).thenReturn(mockMessages);

        // テスト対象のメソッドを実行 
        ResponseEntity<List<ChatMessage>> responseEntity = chatController.findChatMessages(senderId, recipientId);

        // ResponseEntityが正常であることを検証
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(mockMessages, responseEntity.getBody());
    }

}
