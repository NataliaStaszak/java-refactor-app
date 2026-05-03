package com.sap.refactoring.web.controller;

import com.sap.refactoring.users.UserDTO;
import com.sap.refactoring.users.UserUpdateDTO;
import com.sap.refactoring.users.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/users")
public class UserController
{
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@PostMapping("")
	public ResponseEntity addUser(@Valid @RequestBody UserDTO userDTO) {
		userService.createUser(userDTO);
		return ResponseEntity.ok(userDTO);
	}

	@GetMapping("")
	public ResponseEntity getUsers(@RequestParam(value = "name", required = false) String name) {
		if (name != null) {
			return ResponseEntity.ok(userService.findAllUsersByName(name));
		}
		return ResponseEntity.ok(userService.findAllUsers());
	}

	@PatchMapping("{email}")
	public ResponseEntity<UserDTO> updateUser(@PathVariable String email, @Valid @RequestBody UserUpdateDTO userDTO) {
		return ResponseEntity.ok(userService.updateUser(email, userDTO));
	}

	@DeleteMapping("{email}")
	public ResponseEntity deleteUser(@PathVariable String email) {
		userService.deleteUser(email);
		return ResponseEntity.noContent().build();
	}




}
