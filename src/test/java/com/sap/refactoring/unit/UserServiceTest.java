package com.sap.refactoring.unit;

import com.sap.refactoring.users.SaveUserDTO;
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
        SaveUserDTO dto = new SaveUserDTO("john@test.com", "John", List.of("user"));
        when(repository.existsByEmail("john@test.com")).thenReturn(false);
        when(repository.save(any())).thenReturn(new User(1L, "john@test.com", "John", List.of("user")));

        UserDTO result = userService.createUser(dto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@test.com");
        verify(repository).save(any(User.class));
    }

    @Test
    void createUser_throwsDuplicateEmailException_whenEmailExists() {
        SaveUserDTO dto = new SaveUserDTO("john@test.com", "John", List.of("user"));
        when(repository.existsByEmail("john@test.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.createUser(dto));
        verify(repository, never()).save(any());
    }

    @Test
    void createUser_throwsNoRoleException_whenRolesEmpty() {
        SaveUserDTO dto = new SaveUserDTO("john@test.com", "John", List.of());

        assertThrows(NoRoleException.class, () -> userService.createUser(dto));
        verify(repository, never()).save(any());
    }

    @Test
    void createUser_throwsNoRoleException_whenRolesNull() {
        SaveUserDTO dto = new SaveUserDTO("john@test.com", "John", null);

        assertThrows(NoRoleException.class, () -> userService.createUser(dto));
        verify(repository, never()).save(any());
    }

    // --- updateUser ---

    @Test
    void updateUser_updatesNameAndRoles_whenValid() {
        User existing = new User(1L, "john@test.com", "John", List.of("user"));
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        UserUpdateDTO update = new UserUpdateDTO(null, "Updated", List.of("admin"));
        UserDTO result = userService.updateUser(1L, update);

        assertThat(result.getName()).isEqualTo("Updated");
        assertThat(result.getRoles()).containsExactly("admin");
    }

    @Test
    void updateUser_updatesEmail_whenEmailProvided() {
        User existing = new User(1L, "john@test.com", "John", List.of("user"));
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(new User(1L, "new@test.com", "John", List.of("user")));

        UserUpdateDTO update = new UserUpdateDTO("new@test.com", null, null);
        UserDTO result = userService.updateUser(1L, update);

        assertThat(result.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void updateUser_keepsExistingName_whenNameIsNull() {
        User existing = new User(1L, "john@test.com", "John", List.of("user"));
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(existing);

        UserUpdateDTO update = new UserUpdateDTO(null, null, List.of("admin"));
        UserDTO result = userService.updateUser(1L, update);

        assertThat(result.getName()).isEqualTo("John");
    }

    @Test
    void updateUser_throwsUserNotFoundException_whenUserDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(99L, new UserUpdateDTO(null, "Name", List.of("user"))));
    }

    // --- deleteUser ---

    @Test
    void deleteUser_deletesUser_whenUserExists() {
        when(repository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteUser_throwsUserNotFoundException_whenUserDoesNotExist() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99L));
        verify(repository, never()).deleteById(any());
    }

    // --- findAllUsers ---

    @Test
    void findAllUsers_returnsMappedDTOs() {
        when(repository.findAll()).thenReturn(List.of(
                new User(1L, "john@test.com", "John", List.of("user")),
                new User(2L, "jane@test.com", "Jane", List.of("admin"))
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
                new User(1L, "john1@test.com", "John", List.of("user")),
                new User(2L, "john2@test.com", "John", List.of("admin"))
        ));

        List<UserDTO> result = userService.findAllUsersByName("John");

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(u -> u.getName().equals("John"));
    }
}
