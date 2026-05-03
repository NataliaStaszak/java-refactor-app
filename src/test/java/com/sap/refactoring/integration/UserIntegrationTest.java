package com.sap.refactoring.integration;

import com.sap.refactoring.users.UserDTO;
import com.sap.refactoring.users.UserUpdateDTO;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate.getRestTemplate().setRequestFactory(
                new HttpComponentsClientHttpRequestFactory(HttpClients.createDefault()));
    }

    // --- create ---

    @Test
    public void createUser_returnsOk_whenValid() {
        ResponseEntity<UserDTO> response = createUser("john@test.com", "John", "user");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmail()).isEqualTo("john@test.com");
        assertThat(response.getBody().getName()).isEqualTo("John");
        assertThat(response.getBody().getRoles()).containsExactly("user");
    }

    @Test
    public void createUser_returnsConflict_whenDuplicateEmail() {
        createUser("john@test.com", "John", "user");

        ResponseEntity<String> response = restTemplate.postForEntity("/users",
                new UserDTO("john@test.com", "John", List.of("user")), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    public void createUser_returnsBadRequest_whenNoRole() {
        ResponseEntity<String> response = restTemplate.postForEntity("/users",
                new UserDTO("john@test.com", "John", List.of()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // --- update ---

    @Test
    public void updateUser_returnsOk_whenValid() {
        createUser("john@test.com", "John", "user");

        UserUpdateDTO update = new UserUpdateDTO("Updated Name", List.of("admin"));
        ResponseEntity<UserDTO> response = restTemplate.exchange(
                "/users/john@test.com", HttpMethod.PATCH, new HttpEntity<>(update), UserDTO.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Updated Name");
        assertThat(response.getBody().getRoles()).containsExactly("admin");
    }

    @Test
    public void updateUser_returnsNotFound_whenUserDoesNotExist() {
        UserUpdateDTO update = new UserUpdateDTO("Updated Name", List.of("admin"));

        ResponseEntity<String> response = restTemplate.exchange(
                "/users/nonexistent@test.com", HttpMethod.PATCH, new HttpEntity<>(update), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- get ---

    @Test
    public void getUsers_returnsAllUsers() {
        createUser("first@test.com", "First", "user");
        createUser("second@test.com", "Second", "admin");

        ResponseEntity<UserDTO[]> response = restTemplate.getForEntity("/users", UserDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    public void getUsers_returnsEmptyList_whenNoUsers() {
        ResponseEntity<UserDTO[]> response = restTemplate.getForEntity("/users", UserDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    public void getUsers_byName_returnsFilteredUsers() {
        createUser("john1@test.com", "John", "user");
        createUser("john2@test.com", "John", "user");
        createUser("jane@test.com", "Jane", "user");

        ResponseEntity<UserDTO[]> response = restTemplate.getForEntity("/users?name=John", UserDTO[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).allMatch(u -> u.getName().equals("John"));
    }

    // --- delete ---

    @Test
    public void deleteUser_returnsNoContent_whenUserExists() {
        createUser("john@test.com", "John", "user");

        ResponseEntity<Void> response = restTemplate.exchange(
                "/users/john@test.com", HttpMethod.DELETE, null, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<UserDTO[]> getResponse = restTemplate.getForEntity("/users", UserDTO[].class);
        assertThat(getResponse.getBody()).isEmpty();
    }

    @Test
    public void deleteUser_returnsNotFound_whenUserDoesNotExist() {
        ResponseEntity<String> response = restTemplate.exchange(
                "/users/nonexistent@test.com", HttpMethod.DELETE, null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // --- helper ---

    private ResponseEntity<UserDTO> createUser(String email, String name, String... roles) {
        return restTemplate.postForEntity("/users", new UserDTO(email, name, List.of(roles)), UserDTO.class);
    }
}
