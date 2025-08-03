package com.example.demo.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Model.User;


public interface UserService extends JpaRepository<User, Long> {
    User findByUsernameAndPassword(String username, String password);
    User findByUsername(String username); 

}




