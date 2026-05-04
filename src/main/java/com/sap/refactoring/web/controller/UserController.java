package com.sap.refactoring.web.controller;

import com.sap.refactoring.users.SaveUserDTO;
import com.sap.refactoring.users.UserDTO;
import com.sap.refactoring.users.UserUpdateDTO;
import com.sap.refactoring.users.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/users")
public class UserController
{
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("")
	public ResponseEntity<UserDTO> addUser(@Valid @RequestBody SaveUserDTO userDTO) {
		return ResponseEntity.ok(userService.createUser(userDTO));
	}

	@GetMapping("")
	public ResponseEntity<List<UserDTO>> getUsers(@RequestParam(value = "name", required = false) String name) {
		if (name != null) {
			return ResponseEntity.ok(userService.findAllUsersByName(name));
		}
		return ResponseEntity.ok(userService.findAllUsers());
	}

	@PatchMapping("{id}")
	public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO userDTO) {
		return ResponseEntity.ok(userService.updateUser(id, userDTO));
	}

	@DeleteMapping("{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}




}
