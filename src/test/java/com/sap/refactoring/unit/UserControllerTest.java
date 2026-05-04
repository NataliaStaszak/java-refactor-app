package com.sap.refactoring.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.refactoring.users.SaveUserDTO;
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
        SaveUserDTO dto = new SaveUserDTO("john@test.com", "John", List.of("user"));
        when(userService.createUser(any())).thenReturn(new UserDTO(1L, "john@test.com", "John", List.of("user")));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void addUser_returns409_whenDuplicateEmail() throws Exception {
        SaveUserDTO dto = new SaveUserDTO("john@test.com", "John", List.of("user"));
        doThrow(new DuplicateEmailException("john@test.com")).when(userService).createUser(any());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void addUser_returns400_whenNoRole() throws Exception {
        SaveUserDTO dto = new SaveUserDTO("john@test.com", "John", List.of());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addUser_returns400_whenNameBlank() throws Exception {
        SaveUserDTO dto = new SaveUserDTO("john@test.com", "", List.of("user"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // --- GET /users ---

    @Test
    void getUsers_returns200_withList() throws Exception {
        when(userService.findAllUsers()).thenReturn(List.of(
                new UserDTO(1L, "john@test.com", "John", List.of("user"))
        ));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john@test.com"));
    }

    @Test
    void getUsers_byName_returns200_withFilteredList() throws Exception {
        when(userService.findAllUsersByName("John")).thenReturn(List.of(
                new UserDTO(1L, "john@test.com", "John", List.of("user"))
        ));

        mockMvc.perform(get("/users?name=John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John"));
    }

    // --- PATCH /users/{id} ---

    @Test
    void updateUser_returns200_whenValid() throws Exception {
        UserUpdateDTO update = new UserUpdateDTO(null, "Updated", List.of("admin"));
        when(userService.updateUser(eq(1L), any()))
                .thenReturn(new UserDTO(1L, "john@test.com", "Updated", List.of("admin")));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void updateUser_returns404_whenNotFound() throws Exception {
        UserUpdateDTO update = new UserUpdateDTO(null, "Updated", List.of("admin"));
        when(userService.updateUser(eq(99L), any()))
                .thenThrow(new UserNotFoundException(99L));

        mockMvc.perform(patch("/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    // --- DELETE /users/{id} ---

    @Test
    void deleteUser_returns204_whenExists() throws Exception {
        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_returns404_whenNotFound() throws Exception {
        doThrow(new UserNotFoundException(99L))
                .when(userService).deleteUser(99L);

        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound());
    }
}
