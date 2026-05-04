package com.sap.refactoring.users;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String email;

	private String name;

	@ElementCollection
	@CollectionTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_id"))
	@Column(name = "roles")
	private List<String> roles;

	public User(String email, String name, List<String> roles) {
		this.email = email;
		this.name = name;
		this.roles = roles;
	}
}
