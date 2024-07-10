package com.example.one_on_one_chat_app_websocket.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.example.one_on_one_chat_app_websocket.domain.Status;
import com.example.one_on_one_chat_app_websocket.domain.User;

public interface UserRepository extends MongoRepository<User, String> {
    List<User> findAllByStatus(Status status);
}
