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

    public void createUser(UserDTO request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            throw new NoRoleException();
        }
        repository.save(new User(request.getEmail(), request.getName(), request.getRoles()));
    }

    public List<UserDTO> findAllUsers() {
        return repository.findAll().stream()
                .map(UserService::map)
                .collect(Collectors.toList());
    }

    public List<UserDTO> findAllUsersByName(String name) {
        return repository.findAllByName(name).stream()
                .map(UserService::map)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUser(String email, UserUpdateDTO request) {
        User user = repository.findById(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            user.setRoles(request.getRoles());
        }

        return map(repository.save(user));
    }

    @Transactional
    public void deleteUser(String email) {
        if (!repository.existsById(email)) {
            throw new UserNotFoundException(email);
        }
        repository.deleteById(email);
    }

    private static UserDTO map(User user){
        return new UserDTO(user.getEmail(), user.getName(), user.getRoles());
    }
}