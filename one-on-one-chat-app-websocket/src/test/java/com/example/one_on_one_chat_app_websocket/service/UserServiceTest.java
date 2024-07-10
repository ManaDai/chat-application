package com.example.one_on_one_chat_app_websocket.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.one_on_one_chat_app_websocket.domain.Status;
import com.example.one_on_one_chat_app_websocket.domain.User;
import com.example.one_on_one_chat_app_websocket.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testSaveUser() {
        User user = new User();
        user.setFullName("test");
        user.setNickName("test_nickname");

        userService.saveUser(user);

        assertEquals(Status.ONLINE, user.getStatus());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testDisconnect() {
        User user = new User();
        user.setFullName("test");
        user.setNickName("test_nickname");
        user.setStatus(Status.ONLINE);

        when(userRepository.findById(user.getNickName())).thenReturn(Optional.of(user));

        userService.disconnect(user);

        assertEquals(Status.OFFLINE, user.getStatus());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testFindConnectedUsers() {
        // Given
        User user1 = new User();
        user1.setFullName("test1");
        user1.setNickName("test1_nickname");
        user1.setStatus(Status.ONLINE);

        User user2 = new User();
        user2.setFullName("test2");
        user2.setNickName("test2_nickname");
        user2.setStatus(Status.ONLINE);

        List<User> onlineUsers = List.of(user1, user2);

        when(userRepository.findAllByStatus(Status.ONLINE)).thenReturn(onlineUsers);

        List<User> result = userService.findConnectedUsers();

        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
        verify(userRepository, times(1)).findAllByStatus(Status.ONLINE);
    }

}
