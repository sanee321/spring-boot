package com.example.userservice.repository;

import com.example.userservice.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveAndFindById() {
        User user = new User("testuser", "test@example.com", "hash", "Test", "User", Set.of("ROLE_USER"));
        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    public void testFindByUsername_Found() {
        User user = new User("testuser", "test@example.com", "hash", "Test", "User", Set.of("ROLE_USER"));
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername("testuser");
        assertTrue(foundUser.isPresent());
        assertEquals("testuser", foundUser.get().getUsername());
    }

    @Test
    public void testFindByUsername_NotFound() {
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testFindByEmail_Found() {
        User user = new User("testuser", "test@example.com", "hash", "Test", "User", Set.of("ROLE_USER"));
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("test@example.com");
        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
    }

    @Test
    public void testFindByEmail_NotFound() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");
        assertFalse(foundUser.isPresent());
    }

    @Test
    public void testFindAll() {
        User user1 = new User("user1", "user1@example.com", "hash1", "First1", "Last1", Set.of("ROLE_USER"));
        User user2 = new User("user2", "user2@example.com", "hash2", "First2", "Last2", Set.of("ROLE_USER"));
        userRepository.save(user1);
        userRepository.save(user2);

        var users = userRepository.findAll();
        assertEquals(2, users.size());
    }

    @Test
    public void testDeleteById() {
        User user = new User("testuser", "test@example.com", "hash", "Test", "User", Set.of("ROLE_USER"));
        User savedUser = userRepository.save(user);

        userRepository.deleteById(savedUser.getId());

        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertFalse(foundUser.isPresent());
    }
}