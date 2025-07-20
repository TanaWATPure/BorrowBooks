package com.example.demo.Controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Repository.BookRepository;
import com.example.demo.Repository.UserRepository;

@RestController
public class SummaryController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/api/summary")
    public Map<String, Long> getSummary() {
        long userCount = userRepository.count();
        long bookCount = bookRepository.count();

        Map<String, Long> result = new HashMap<>();
        result.put("totalUsers", userCount);
        result.put("totalBooks", bookCount);

        return result;
    }

}
