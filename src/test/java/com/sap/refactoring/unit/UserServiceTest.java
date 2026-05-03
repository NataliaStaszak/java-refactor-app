package com.sap.refactoring.unit;

import com.sap.refactoring.users.UserDTO;
import com.sap.refactoring.users.UserUpdateDTO;
import com.sap.refactoring.users.UserRepository;
import com.sap.refactoring.users.UserService;
import com.sap.refactoring.users.User;
import com.sap.refactoring.users.exception.DuplicateEmailException;
import com.sap.refactoring.users.exception.NoRoleException;
import com.sap.refactoring.users.exception.UserNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    UserRepository repository;

    @InjectMocks
    UserService userService;

    // --- createUser ---

    @Test
    void createUser_savesUser_whenValid() {
        UserDTO dto = new UserDTO("john@test.com", "John", List.of("user"));
        when(repository.existsByEmail("john@test.com")).thenReturn(false);

        userService.createUser(dto);

        verify(repository).save(any(User.class));
    }

    @Test
    void createUser_throwsDuplicateEmailException_whenEmailExists() {
        UserDTO dto = new UserDTO("john@test.com", "John", List.of("user"));
        when(repository.existsByEmail("john@test.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.createUser(dto));
        verify(repository, never()).save(any());
    }

    @Test
    void createUser_throwsNoRoleException_whenRolesEmpty() {
        UserDTO dto = new UserDTO("john@test.com", "John", List.of());

        assertThrows(NoRoleException.class, () -> userService.createUser(dto));
        verify(repository, never()).save(any());
    }

    @Test
    void createUser_throwsNoRoleException_whenRolesNull() {
        UserDTO dto = new UserDTO("john@test.com", "John", null);

        assertThrows(NoRoleException.class, () -> userService.createUser(dto));
        verify(repository, never()).save(any());
    }

    // --- updateUser ---

    @Test
    void updateUser_updatesNameAndRoles_whenValid() {
        User existing = new User("john@test.com", "John", List.of("user"));
        when(repository.findById("john@test.com")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        UserUpdateDTO update = new UserUpdateDTO("Updated", List.of("admin"));
        UserDTO result = userService.updateUser("john@test.com", update);

        assertThat(result.getName()).isEqualTo("Updated");
        assertThat(result.getRoles()).containsExactly("admin");
    }

    @Test
    void updateUser_keepsExistingName_whenNameIsNull() {
        User existing = new User("john@test.com", "John", List.of("user"));
        when(repository.findById("john@test.com")).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        UserUpdateDTO update = new UserUpdateDTO(null, List.of("admin"));
        UserDTO result = userService.updateUser("john@test.com", update);

        assertThat(result.getName()).isEqualTo("John");
    }

    @Test
    void updateUser_throwsUserNotFoundException_whenUserDoesNotExist() {
        when(repository.findById("nonexistent@test.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser("nonexistent@test.com", new UserUpdateDTO("Name", List.of("user"))));
    }

    // --- deleteUser ---

    @Test
    void deleteUser_deletesUser_whenUserExists() {
        when(repository.existsById("john@test.com")).thenReturn(true);

        userService.deleteUser("john@test.com");

        verify(repository).deleteById("john@test.com");
    }

    @Test
    void deleteUser_throwsUserNotFoundException_whenUserDoesNotExist() {
        when(repository.existsById("nonexistent@test.com")).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser("nonexistent@test.com"));
        verify(repository, never()).deleteById(any());
    }

    // --- findAllUsers ---

    @Test
    void findAllUsers_returnsMappedDTOs() {
        when(repository.findAll()).thenReturn(List.of(
                new User("john@test.com", "John", List.of("user")),
                new User("jane@test.com", "Jane", List.of("admin"))
        ));

        List<UserDTO> result = userService.findAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserDTO::getEmail)
                .containsExactly("john@test.com", "jane@test.com");
    }

    // --- findAllUsersByName ---

    @Test
    void findAllUsersByName_returnsFilteredDTOs() {
        when(repository.findAllByName("John")).thenReturn(List.of(
                new User("john1@test.com", "John", List.of("user")),
                new User("john2@test.com", "John", List.of("admin"))
        ));

        List<UserDTO> result = userService.findAllUsersByName("John");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(u -> u.getName().equals("John"));
    }
}
