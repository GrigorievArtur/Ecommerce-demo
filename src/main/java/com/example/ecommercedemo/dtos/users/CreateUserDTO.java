package com.example.ecommercedemo.dtos.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {
        private String firstname;
        private String lastname;
        private String password;
        private String email;
}
