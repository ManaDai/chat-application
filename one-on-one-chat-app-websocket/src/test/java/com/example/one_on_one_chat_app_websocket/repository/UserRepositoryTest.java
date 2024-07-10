package com.example.one_on_one_chat_app_websocket.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.example.one_on_one_chat_app_websocket.domain.Status;
import com.example.one_on_one_chat_app_websocket.domain.User;


@DataMongoTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    public void setUp() {
        user1 = new User();
        user1.setStatus(Status.ONLINE);
        user1.setFullName("test1");
        user1.setNickName("test1_nickname");

        user2 = new User();
        user2.setStatus(Status.ONLINE);
        user2.setFullName("test2");
        user2.setNickName("test2_nickname");

        user3 = new User();
        user3.setStatus(Status.OFFLINE);
        user3.setFullName("test3");
        user3.setNickName("test3_nickname");

        mongoTemplate.save(user1);
        mongoTemplate.save(user2);
        mongoTemplate.save(user3);
    }

    @AfterEach
    public void tearDown() {
        mongoTemplate.remove(Query.query(Criteria.where("fullName").is("test1")), User.class);
        mongoTemplate.remove(Query.query(Criteria.where("fullName").is("test2")), User.class);
        mongoTemplate.remove(Query.query(Criteria.where("fullName").is("test3")), User.class);
    }

    @Test
    void testFindAllByStatus() {
        List<User> users = userRepository.findAllByStatus(Status.ONLINE);
        assertEquals(2, users.size(), "Number of users should be 2");
        assertEquals("test1", users.get(0).getFullName());
        assertEquals("test2", users.get(1).getFullName());
    }
}