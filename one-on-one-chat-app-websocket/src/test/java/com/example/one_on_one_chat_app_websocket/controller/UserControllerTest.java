package com.example.one_on_one_chat_app_websocket.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.one_on_one_chat_app_websocket.domain.Status;
import com.example.one_on_one_chat_app_websocket.domain.User;
import com.example.one_on_one_chat_app_websocket.repository.UserRepository;
import com.example.one_on_one_chat_app_websocket.service.UserService;


@SpringBootTest
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

   
    @Test
    public void testAddUser() {
        // テスト用のユーザーオブジェクト作成
        User user = new User();
        user.setNickName("john_doe");
        user.setFullName("John Doe");
        user.setStatus(Status.ONLINE); // 仮定されるステータスの設定

        // saveUserメソッドのモック動作を設定
        doNothing().when(userService).saveUser(any(User.class));
        

        // addUserメソッドを実行
        User result = userController.addUser(user);

        // 返されたユーザーが期待通りであることを検証
        assertEquals(user.getNickName(), result.getNickName());
        assertEquals(user.getFullName(), result.getFullName());
        assertEquals(user.getStatus(), result.getStatus()); // ステータスの検証
        // verify(userService, times(1)).saveUser(any(User.class));
    }

    @Test
    public void testDisconnectUser() {
        // テスト用のユーザーオブジェクト作成
        User user = new User();
        user.setNickName("john_doe");
        user.setFullName("John Doe");
        user.setStatus(Status.ONLINE); // 仮定されるステータスの設定

        // disconnectメソッドのモック動作を設定
        doNothing().when(userService).disconnect(any(User.class));

        // disconnectUserメソッドを実行
        User result = userController.disconnectUser(user);

        // 返されたユーザーが期待通りであることを検証
        assertEquals(user.getNickName(), result.getNickName());
        assertEquals(user.getFullName(), result.getFullName());
        assertEquals(user.getStatus(), result.getStatus()); // ステータスの検証
        // verify(userService, times(1)).disconnect(any(User.class));
    }

    @Test
    public void testFindConnectedUsers() {
        // テスト用のユーザーリスト作成
        List<User> users = Arrays.asList(
                createUser("john_doe", "John Doe", Status.ONLINE),
                createUser("jane_smith", "Jane Smith", Status.ONLINE));

        // findConnectedUsersメソッドのモック動作を設定
        when(userService.findConnectedUsers()).thenReturn(users);

        // findConnectedUsersメソッドを実行
        ResponseEntity<List<User>> responseEntity = userController.findConnectedUsers();

        // ResponseEntityが正常であることを検証
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    private User createUser(String nickName, String fullName, Status status) {
        User user = new User();
        user.setNickName(nickName);
        user.setFullName(fullName);
        user.setStatus(status);
        return user;
    }
}
