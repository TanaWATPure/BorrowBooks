package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.Model.Book;
import com.example.demo.Model.User;
import com.example.demo.Repository.BookRepository;
import com.example.demo.Repository.BorrowRepository;
import com.example.demo.Repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private BookRepository bookRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private BorrowRepository borrowRepo;

    @GetMapping("/dashboard/{userId}")
    public String dashboard(@PathVariable Long userId, HttpSession session, Model model) {

        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }

        if (!sessionUser.getId().equals(userId)) {
            return "redirect:/login";
        }

        if (!"admin".equalsIgnoreCase(sessionUser.getRole())) {
            return "redirect:/access-denied"; 
        }

        User user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            return "error"; 
        }

        model.addAttribute("books", bookRepo.findAll());
        model.addAttribute("book", new Book());
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("borrows", borrowRepo.findAll());

        model.addAttribute("totalUsers", userRepo.count());
        model.addAttribute("totalBooks", bookRepo.count());
        model.addAttribute("borrowedCount", borrowRepo.count());

        return "admin-dashboard";
    }



}
