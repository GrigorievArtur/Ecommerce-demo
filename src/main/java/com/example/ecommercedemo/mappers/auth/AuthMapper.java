package com.example.ecommercedemo.mappers.auth;

import com.example.ecommercedemo.dtos.auth.RegisterDTO;
import com.example.ecommercedemo.dtos.users.CreateUserDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    CreateUserDTO getCreateUserDTO(RegisterDTO registerDTO);
}
