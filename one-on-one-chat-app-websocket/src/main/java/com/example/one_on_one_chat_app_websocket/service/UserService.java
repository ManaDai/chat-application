package com.example.one_on_one_chat_app_websocket.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.one_on_one_chat_app_websocket.domain.Status;
import com.example.one_on_one_chat_app_websocket.domain.User;
import com.example.one_on_one_chat_app_websocket.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;

    public void saveUser(User user) {
        user.setStatus(Status.ONLINE);
        repository.save(user);
    }

    public void disconnect(User user) {
        var storedUser = repository.findById(user.getNickName()).orElse(null);
        if (storedUser != null) {
            storedUser.setStatus(Status.OFFLINE);
            repository.save(storedUser);
        }
    }

    public List<User> findConnectedUsers() {
        return repository.findAllByStatus(Status.ONLINE);
    }
}