package com.example.userservice.service;

import com.example.userservice.entity.User;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void testGetAllUsers() {
        User user1 = new User("user1", "user1@example.com", "hash1", "First1", "Last1", Set.of("ROLE_USER"));
        User user2 = new User("user2", "user2@example.com", "hash2", "First2", "Last2", Set.of("ROLE_USER"));
        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testGetUserById_Found() {
        User user = new User("user1", "user1@example.com", "hash1", "First1", "Last1", Set.of("ROLE_USER"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getUsername());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(1L);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testCreateUser() {
        User user = new User("user1", "user1@example.com", "hash1", "First1", "Last1", Set.of("ROLE_USER"));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertEquals(user, result);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testUpdateUser_Found() {
        User existingUser = new User("user1", "user1@example.com", "hash1", "First1", "Last1", Set.of("ROLE_USER"));
        existingUser.setId(1L);
        User userDetails = new User("updated", "updated@example.com", "newhash", "Updated", "User", Set.of("ROLE_ADMIN"));
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updateUser(1L, userDetails);

        assertNotNull(result);
        assertEquals("updated", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    public void testUpdateUser_NotFound() {
        User userDetails = new User("updated", "updated@example.com", "newhash", "Updated", "User", Set.of("ROLE_ADMIN"));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        User result = userService.updateUser(1L, userDetails);

        assertNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testGetUserByUsername_Found() {
        User user = new User("user1", "user1@example.com", "hash1", "First1", "Last1", Set.of("ROLE_USER"));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByUsername("user1");

        assertTrue(result.isPresent());
        assertEquals("user1", result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("user1");
    }

    @Test
    public void testGetUserByUsername_NotFound() {
        when(userRepository.findByUsername("user1")).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserByUsername("user1");

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("user1");
    }

    @Test
    public void testGetUserByEmail_Found() {
        User user = new User("user1", "user1@example.com", "hash1", "First1", "Last1", Set.of("ROLE_USER"));
        when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserByEmail("user1@example.com");

        assertTrue(result.isPresent());
        assertEquals("user1@example.com", result.get().getEmail());
        verify(userRepository, times(1)).findByEmail("user1@example.com");
    }

    @Test
    public void testGetUserByEmail_NotFound() {
        when(userRepository.findByEmail("user1@example.com")).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserByEmail("user1@example.com");

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByEmail("user1@example.com");
    }
}