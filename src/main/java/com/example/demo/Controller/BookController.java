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
import com.example.demo.Repository.BookRepository;

@Controller
@RequestMapping("/admin/books")
public class BookController {

    @Autowired
    private BookRepository bookRepo;

    // ✅ แสดงรายการหนังสือ
    @GetMapping
    public String listBooks(Model model) {
        model.addAttribute("books", bookRepo.findAll());
        model.addAttribute("book", new Book());
        return "books";
    }

    // ✅ เพิ่มหนังสือ
    @PostMapping("/add")
    public String addBook(@ModelAttribute Book book) {
        bookRepo.save(book);
        return "redirect:/admin/dashboard";
    }

    // ✅ แก้ไขหนังสือ
    @PostMapping("/edit")
    public String editBook(@ModelAttribute Book book) {
        bookRepo.save(book);
        return "redirect:/admin/dashboard";
    }

    // ✅ ลบหนังสือ (ใช้ GET เพื่อให้ทำงานกับ <a href>)
    @GetMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookRepo.deleteById(id);
        return "redirect:/admin/dashboard";
    }
}
