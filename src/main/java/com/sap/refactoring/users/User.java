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
	private String email;
	private String name;
	@ElementCollection
	@CollectionTable(name = "users_roles", joinColumns = @JoinColumn(name = "user_email"))
	@Column(name = "roles")
	private List<String> roles;
}
