package com.sap.refactoring.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.refactoring.users.UserDTO;
import com.sap.refactoring.users.UserUpdateDTO;
import com.sap.refactoring.users.UserService;
import com.sap.refactoring.users.exception.DuplicateEmailException;
import com.sap.refactoring.users.exception.UserNotFoundException;
import com.sap.refactoring.web.controller.UserController;
import com.sap.refactoring.web.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
public class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    UserService userService;

    // --- POST /users ---

    @Test
    void addUser_returns200_whenValid() throws Exception {
        UserDTO dto = new UserDTO("john@test.com", "John", List.of("user"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void addUser_returns409_whenDuplicateEmail() throws Exception {
        UserDTO dto = new UserDTO("john@test.com", "John", List.of("user"));
        doThrow(new DuplicateEmailException("john@test.com")).when(userService).createUser(any());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void addUser_returns400_whenNoRole() throws Exception {
        UserDTO dto = new UserDTO("john@test.com", "John", List.of());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_returns400_whenNameBlank() throws Exception {
        UserDTO dto = new UserDTO("john@test.com", "", List.of("user"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // --- GET /users ---

    @Test
    void getUsers_returns200_withList() throws Exception {
        when(userService.findAllUsers()).thenReturn(List.of(
                new UserDTO("john@test.com", "John", List.of("user"))
        ));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john@test.com"));
    }

    @Test
    void getUsers_byName_returns200_withFilteredList() throws Exception {
        when(userService.findAllUsersByName("John")).thenReturn(List.of(
                new UserDTO("john@test.com", "John", List.of("user"))
        ));

        mockMvc.perform(get("/users?name=John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John"));
    }

    // --- PATCH /users/{email} ---

    @Test
    void updateUser_returns200_whenValid() throws Exception {
        UserUpdateDTO update = new UserUpdateDTO("Updated", List.of("admin"));
        when(userService.updateUser(eq("john@test.com"), any()))
                .thenReturn(new UserDTO("john@test.com", "Updated", List.of("admin")));

        mockMvc.perform(patch("/users/john@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void updateUser_returns404_whenNotFound() throws Exception {
        UserUpdateDTO update = new UserUpdateDTO("Updated", List.of("admin"));
        when(userService.updateUser(eq("nonexistent@test.com"), any()))
                .thenThrow(new UserNotFoundException("nonexistent@test.com"));

        mockMvc.perform(patch("/users/nonexistent@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_returns400_whenNameIsBlank() throws Exception {
        UserUpdateDTO update = new UserUpdateDTO("", List.of("admin"));

        mockMvc.perform(patch("/users/john@test.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isBadRequest());
    }

    // --- DELETE /users/{email} ---

    @Test
    void deleteUser_returns204_whenExists() throws Exception {
        mockMvc.perform(delete("/users/john@test.com"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser("john@test.com");
    }

    @Test
    void deleteUser_returns404_whenNotFound() throws Exception {
        doThrow(new UserNotFoundException("nonexistent@test.com"))
                .when(userService).deleteUser("nonexistent@test.com");

        mockMvc.perform(delete("/users/nonexistent@test.com"))
                .andExpect(status().isNotFound());
    }
}
