package com.sap.refactoring.users;

import com.sap.refactoring.users.exception.DuplicateEmailException;
import com.sap.refactoring.users.exception.NoRoleException;
import com.sap.refactoring.users.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserDTO createUser(SaveUserDTO newSaveUserDTO) {
        if (repository.existsByEmail(newSaveUserDTO.getEmail())) {
            throw new DuplicateEmailException(newSaveUserDTO.getEmail());
        }
        if (newSaveUserDTO.getRoles() == null || newSaveUserDTO.getRoles().isEmpty()) {
            throw new NoRoleException();
        }
        User saved = repository.save(new User(newSaveUserDTO.getEmail(), newSaveUserDTO.getName(), newSaveUserDTO.getRoles()));
        return mapUser2UserDTO(saved);
    }

    public List<UserDTO> findAllUsers() {
        return repository.findAll().stream()
                .map(UserService::mapUser2UserDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> findAllUsersByName(String name) {
        return repository.findAllByName(name).stream()
                .map(UserService::mapUser2UserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUser(Long id, UserUpdateDTO userUpdateDTO) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (userUpdateDTO.getEmail() != null) {
            user.setEmail(userUpdateDTO.getEmail());
        }
        if (userUpdateDTO.getName() != null) {
            user.setName(userUpdateDTO.getName());
        }
        if (userUpdateDTO.getRoles() != null && !userUpdateDTO.getRoles().isEmpty()) {
            user.setRoles(userUpdateDTO.getRoles());
        }

        return mapUser2UserDTO(repository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        repository.deleteById(id);

    }

    private static UserDTO mapUser2UserDTO(User user){
        return new UserDTO(user.getId(),user.getEmail(), user.getName(), user.getRoles());
    }
}