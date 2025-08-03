package com.example.demo.Controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Model.Book;
import com.example.demo.Model.Borrow;
import com.example.demo.Model.User;
import com.example.demo.Service.BookService;
import com.example.demo.Service.BorrowService;
import com.example.demo.Service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired private BookService bookRepo;
    @Autowired private UserService userRepo;
    @Autowired private BorrowService borrowRepo;

    private boolean isAdmin(User user) {
        return user != null && "admin".equalsIgnoreCase(user.getRole());
    }

    @GetMapping("/dashboard/{userId}")
    public String dashboard(@PathVariable Long userId, HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");

        if (!isAdmin(sessionUser) || !sessionUser.getId().equals(userId)) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("books", bookRepo.findAll());
        model.addAttribute("book", new Book());
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("borrows", borrowRepo.findAll());

        model.addAttribute("totalUsers", userRepo.count());
        model.addAttribute("totalBooks", bookRepo.count());
        model.addAttribute("borrowedCount", borrowRepo.count());

        return "admin/admin-dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (!isAdmin(sessionUser)) {
            return "redirect:/login";
        }

        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("newUser", new User());
        return "admin/users";
    }

    @PostMapping("/users/add")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam(defaultValue = "user") String role,
                          HttpSession session) {

        User sessionUser = (User) session.getAttribute("user");
        if (!isAdmin(sessionUser)) {
            return "redirect:/login";
        }

        if (userRepo.findByUsername(username) != null) {
            return "redirect:/admin/users?error=duplicate";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        userRepo.save(user);
        return "redirect:/admin/users";
    }

    // ==================== Book Management ====================//

    @GetMapping("/books")
    public String listBooks(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (!isAdmin(sessionUser)) {
            return "redirect:/login";
        }

        model.addAttribute("books", bookRepo.findAll());
        model.addAttribute("book", new Book());
        return "admin/books";
    }

   @PostMapping("/books/add")
    public String addBook(@RequestParam String title,
                        @RequestParam String author,
                        @RequestParam int quantity,
                        @RequestParam String description,
                        @RequestParam("image") MultipartFile image,
                        HttpSession session) throws IOException {
        User sessionUser = (User) session.getAttribute("user");
        if (!isAdmin(sessionUser)) {
            return "redirect:/login";
        }

        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setQuantity(quantity);
        book.setDescription(description);

        if (!image.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path uploadPath = Paths.get("uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {
                Path filePath = uploadPath.resolve(filename);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                book.setImageUrl("uploads/" + filename);
            }
        }

        bookRepo.save(book);

        return "redirect:/admin/books";
    }



   @PostMapping("/books/edit")
    public String editBook(@RequestParam Long id,
                        @RequestParam String title,
                        @RequestParam String author,
                        @RequestParam int quantity,
                        @RequestParam String description,
                        @RequestParam(value = "image", required = false) MultipartFile image,
                        HttpSession session) throws IOException {
        User sessionUser = (User) session.getAttribute("user");
        if (!isAdmin(sessionUser)) {
            return "redirect:/login";
        }

        Book book = bookRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid book ID"));
        book.setTitle(title);
        book.setAuthor(author);
        book.setQuantity(quantity);
        book.setDescription(description);

        if (image != null && !image.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + image.getOriginalFilename();
            Path uploadPath = Paths.get("uploads");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = image.getInputStream()) {
                Path filePath = uploadPath.resolve(filename);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                book.setImageUrl("uploads/" + filename);
            }
        }

        bookRepo.save(book);
        return "redirect:/admin/books";
    }

    @GetMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (!isAdmin(sessionUser)) {
            return "redirect:/login";
        }

        bookRepo.deleteById(id);
        return "redirect:/admin/books";
    }

    @PostMapping("/books/update-return-date/{borrowId}")
    public String updateReturnDate(@PathVariable Long borrowId,
                                   @RequestParam("returnDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime returnDate,
                                   HttpSession session) {

        User sessionUser = (User) session.getAttribute("user");
        if (!isAdmin(sessionUser)) {
            return "redirect:/login";
        }

        Borrow borrow = borrowRepo.findById(borrowId).orElse(null);
        if (borrow != null && !borrow.isReturned()) {
            borrow.setReturnDate(returnDate);
            borrow.setReturned(true);

            LocalDateTime dueDate = borrow.getDueDate();
            if (dueDate != null && returnDate.isAfter(dueDate)) {
                long lateDays = ChronoUnit.DAYS.between(dueDate.toLocalDate(), returnDate.toLocalDate());
                borrow.setFine(lateDays * 10.0);
            }

            borrowRepo.save(borrow);

            Book book = borrow.getBook();
            book.setQuantity(book.getQuantity() + borrow.getQuantity());
            bookRepo.save(book);
        }

        return "redirect:/admin/dashboard/" + sessionUser.getId();
    }

    @GetMapping("/borrows")
    public String listBorrows(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (!isAdmin(sessionUser)) {
            return "redirect:/login";
        }

        model.addAttribute("borrows", borrowRepo.findAll());
        return "admin/borrows";
    }
}
