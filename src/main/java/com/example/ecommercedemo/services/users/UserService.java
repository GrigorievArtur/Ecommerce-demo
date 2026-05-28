package com.example.ecommercedemo.services.users;

import com.example.ecommercedemo.dtos.users.CreateUserDTO;
import com.example.ecommercedemo.entities.users.User;
import com.example.ecommercedemo.mappers.users.UserMapper;
import com.example.ecommercedemo.repositories.users.UserRepo;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    public User createUser(CreateUserDTO dto) {
        var user = userMapper.toUser(dto);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        return userRepo.save(user);
    }

    public void deleteUser(Long userId, UserDetails userDetails) {
        userRepo.deleteByIdAndEmail(userId, userDetails.getUsername());
    }

    @Override
    public UserDetails loadUserByUsername(@NotNull String username) throws UsernameNotFoundException {
        return userRepo.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
