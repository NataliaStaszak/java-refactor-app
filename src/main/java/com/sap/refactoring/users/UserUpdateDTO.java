package com.sap.refactoring.users;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDTO {

    @jakarta.validation.constraints.NotBlank(message = "Name must not be blank")
    private String name;

    @Size(min = 1, message = "User must have at least one role")
    private List<String> roles;
}
