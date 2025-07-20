package com.example.demo.Controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Model.Book;
import com.example.demo.Model.Borrow;
import com.example.demo.Model.User;
import com.example.demo.Repository.BookRepository;
import com.example.demo.Repository.BorrowRepository;
import com.example.demo.Repository.UserRepository;


@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired private UserRepository userRepo;
    @Autowired private BookRepository bookRepo;
    @Autowired private BorrowRepository borrowRepo;

    @GetMapping("/dashboard/{userId}")
    public String userDashboard(@PathVariable Long userId, Model model) {
        User user = userRepo.findById(userId).orElse(null);
        if(user == null) {
            return "error"; // หรือหน้า error อื่น ๆ
        }

        List<Book> availableBooks = bookRepo.findAll();
        List<Borrow> currentBorrows = borrowRepo.findByUserIdAndReturnedFalse(userId);
        List<Borrow> borrowHistory = borrowRepo.findByUserId(userId);

        model.addAttribute("user", user);
        model.addAttribute("availableBooks", availableBooks);
        model.addAttribute("currentBorrows", currentBorrows);
        model.addAttribute("borrowHistory", borrowHistory);
        model.addAttribute("newBorrow", new Borrow()); 

        return "user-dashboard";
    }

    // POST ยืมหนังสือ
    @PostMapping("/borrow/{userId}")
    public String borrowBook(@PathVariable Long userId, @RequestParam Long bookId) {
        User user = userRepo.findById(userId).orElse(null);
        Book book = bookRepo.findById(bookId).orElse(null);

        if(user == null || book == null || book.getQuantity() <= 0) {
            return "redirect:/user/dashboard/" + userId + "?error";
        }

        // สร้าง record ยืม
        Borrow borrow = new Borrow();
        borrow.setUser(user);
        borrow.setBook(book);
        borrow.setBorrowDate(LocalDate.now());
        borrow.setReturned(false);
        borrow.setFine(0.0);

        borrowRepo.save(borrow);

        // ลดจำนวนหนังสือ
        book.setQuantity(book.getQuantity() - 1);
        bookRepo.save(book);

        return "redirect:/user/dashboard/" + userId;
    }

    // POST คืนหนังสือ
    @PostMapping("/return/{borrowId}")
    public String returnBook(@PathVariable Long borrowId) {
        Borrow borrow = borrowRepo.findById(borrowId).orElse(null);
        if (borrow == null) {
            return "redirect:/user/dashboard"; 
        }
        if (borrow.isReturned()) {
            return "redirect:/user/dashboard/" + borrow.getUser().getId();
        }

        borrow.setReturned(true);
        borrow.setReturnDate(LocalDate.now());

        long daysBorrowed = java.time.temporal.ChronoUnit.DAYS.between(borrow.getBorrowDate(), borrow.getReturnDate());
        if (daysBorrowed > 7) {
            borrow.setFine((daysBorrowed - 7) * 10);
        } else {
            borrow.setFine(0.0);
        }

        borrowRepo.save(borrow);

        Book book = borrow.getBook();
        book.setQuantity(book.getQuantity() + 1);
        bookRepo.save(book);

        return "redirect:/user/dashboard/" + borrow.getUser().getId();
    }

}
