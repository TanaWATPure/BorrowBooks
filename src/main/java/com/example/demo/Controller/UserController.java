package com.example.demo.Controller;

import java.time.LocalDateTime;
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
import com.example.demo.Service.BookService;
import com.example.demo.Service.BorrowService;
import com.example.demo.Service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired private UserService userRepo;
    @Autowired private BookService bookRepo;
    @Autowired private BorrowService borrowRepo;

    // ==================== 1. หน้าสำหรับยืมหนังสือ (Dashboard หลัก) ====================
   @GetMapping("/dashboard/{userId}")
    public String showBorrowPage(
            @PathVariable Long userId,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model) {
        
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);

        List<Book> books;
        if (keyword != null && !keyword.trim().isEmpty()) {
            books = bookRepo.searchByTitleOrAuthor(keyword.trim());
        } else {
            books = bookRepo.findAll();
        }
        model.addAttribute("availableBooks", books);
        model.addAttribute("keyword", keyword);  // เพื่อให้ส่งกลับไปแสดงในฟอร์มค้นหา

        return "user/user-dashboard";
    }


    // ==================== 2. หน้าสำหรับแสดงหนังสือที่ยืมอยู่ ====================
    @GetMapping("/borrowed/{userId}")
    public String showBorrowedBooksPage(@PathVariable Long userId, Model model) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        // ดึงข้อมูลเฉพาะที่หน้านี้ต้องการ
        model.addAttribute("user", user);
        model.addAttribute("currentBorrows", borrowRepo.findByUserIdAndReturnedFalse(userId));
        return "user/borrowed-books"; 
    }

    // ==================== 3. หน้าสำหรับแสดงประวัติการยืมทั้งหมด ====================
    @GetMapping("/history/{userId}")
    public String showHistoryPage(@PathVariable Long userId, Model model) {
        User user = userRepo.findById(userId).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }
        // ดึงข้อมูลเฉพาะที่หน้านี้ต้องการ
        model.addAttribute("user", user);
        model.addAttribute("borrowHistory", borrowRepo.findByUserId(userId));
        return "user/borrow-history"; 
    }


    // ==================== Action: ยืมหนังสือ ====================
    @PostMapping("/borrow")
    public String borrowBook(
            @RequestParam Long bookId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; 
        }

        Book book = bookRepo.findById(bookId).orElse(null);
        if (book == null || book.getQuantity() < quantity || quantity <= 0) {
            return "redirect:/user/dashboard/" + user.getId() + "?error=borrow_failed";
        }

        Borrow borrow = new Borrow();
        borrow.setUser(user);
        borrow.setBook(book);
        borrow.setBorrowDate(LocalDateTime.now());
        borrow.setDueDate(LocalDateTime.now().plusDays(7));
        borrow.setReturned(false);
        borrow.setFine(0.0);
        borrow.setQuantity(quantity);
        borrowRepo.save(borrow);

        book.setQuantity(book.getQuantity() - quantity);
        bookRepo.save(book);

        return "redirect:/user/dashboard/" + user.getId(); 
    }


    // ==================== Action: คืนหนังสือ ====================
    @PostMapping("/return/{borrowId}")
    public String returnBook(@PathVariable Long borrowId, HttpSession session) {
        Borrow borrow = borrowRepo.findById(borrowId).orElse(null);
        User user = (User) session.getAttribute("user");

        if (borrow == null || user == null) {
            return "redirect:/login";
        }

        if (!borrow.getUser().getId().equals(user.getId())) {
             return "redirect:/logout";
        }
        
        if (borrow.isReturned()) {
            return "redirect:/user/borrowed/" + user.getId();
        }

        LocalDateTime now = LocalDateTime.now();
        borrow.setReturned(true);
        borrow.setReturnDate(now);

        LocalDateTime dueDate = borrow.getDueDate();
        if (dueDate != null && now.isAfter(dueDate)) {
            long lateDays = java.time.temporal.ChronoUnit.DAYS.between(dueDate.toLocalDate(), now.toLocalDate());
            borrow.setFine(lateDays * 10.0);
        } else {
            borrow.setFine(0.0);
        }
        borrowRepo.save(borrow);

        Book book = borrow.getBook();
        book.setQuantity(book.getQuantity() + borrow.getQuantity());
        bookRepo.save(book);

        return "redirect:/user/borrowed/" + user.getId(); 
    }
}