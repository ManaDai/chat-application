package com.example.one_on_one_chat_app_websocket.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.one_on_one_chat_app_websocket.domain.ChatMessage;
import com.example.one_on_one_chat_app_websocket.domain.ChatNotification;
import com.example.one_on_one_chat_app_websocket.service.ChatMessageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {

        private final SimpMessagingTemplate messagingTemplate;
        private final ChatMessageService chatMessageService;

        @Autowired // Optional: Spring Boot 2.5以降は@Autowiredを省略可能
        public ChatController(ChatMessageService chatMessageService, SimpMessagingTemplate messagingTemplate) {
                this.chatMessageService = chatMessageService;
                this.messagingTemplate = messagingTemplate;
        }

        @MessageMapping("/chat")
        public void processMessage(@Payload ChatMessage chatMessage) {
                ChatMessage savedMsg = chatMessageService.save(chatMessage);
                messagingTemplate.convertAndSendToUser(
                                chatMessage.getRecipientId(), "/queue/messages",
                                new ChatNotification(
                                                savedMsg.getId(),
                                                savedMsg.getSenderId(),
                                                savedMsg.getRecipientId(),
                                                savedMsg.getContent(),
                                                savedMsg.getTimestamp()));
        }

        @GetMapping("/messages/{senderId}/{recipientId}")
        public ResponseEntity<List<ChatMessage>> findChatMessages(@PathVariable String senderId,
                        @PathVariable String recipientId) {
                return ResponseEntity
                                .ok(chatMessageService.findChatMessages(senderId, recipientId));
        }

        @DeleteMapping("/messages/{messageId}")
        public ResponseEntity<String> deleteMessage(@PathVariable String messageId) {
                Optional<ChatMessage> chatMessageOptional = chatMessageService.findById(messageId);
                if (chatMessageOptional.isPresent()) {
                        chatMessageService.delete(chatMessageOptional.get());
                        // 削除されたメッセージIDを全ユーザーに通知
                        messagingTemplate.convertAndSend("/topic/messageDeleted", messageId);

                        return ResponseEntity.ok("Message deleted successfully");
                } else {
                        return ResponseEntity.notFound().build();
                }
        }

}
