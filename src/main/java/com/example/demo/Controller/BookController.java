package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.Model.Book;
import com.example.demo.Model.User;
import com.example.demo.Repository.BookRepository;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/books")
public class BookController {

    @Autowired
    private BookRepository bookRepo;

    @GetMapping
    public String listBooks(Model model) {
        model.addAttribute("books", bookRepo.findAll());
        model.addAttribute("book", new Book());
        return "books";
    }

    @PostMapping("/add")
    public String addBook(@ModelAttribute Book book, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        bookRepo.save(book);
        return "redirect:/admin/dashboard/" + sessionUser.getId();
    }

    @PostMapping("/edit")
    public String editBook(@ModelAttribute Book book, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        bookRepo.save(book);
        return "redirect:/admin/dashboard/" + sessionUser.getId();
    }

    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id, HttpSession session) {
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) return "redirect:/login";

        bookRepo.deleteById(id);
        return "redirect:/admin/dashboard/" + sessionUser.getId();
    }
}
