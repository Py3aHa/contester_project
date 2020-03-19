package com.project.SptingSecurity.services;

import com.project.SptingSecurity.Repositories.UserRepositories;
import com.project.SptingSecurity.entities.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepositories userRepositories;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Users user = userRepositories.findByEmail(s);
        User secUser = new User(user.getEmail(), user.getPassword(), user.getRoles());
        return secUser;
    }

    public Users registerUser(Users user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepositories.save(user);
    }
}
