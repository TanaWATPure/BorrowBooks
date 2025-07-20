package com.example.demo.Controller;
import com.example.demo.Model.Book;
import com.example.demo.Model.Borrow;
import com.example.demo.Model.User;
import com.example.demo.Repository.BookRepository;
import com.example.demo.Repository.BorrowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;


@Controller
@RequestMapping("/user/borrow")
public class BorrowController {

    @Autowired BorrowRepository borrowRepo;
    @Autowired BookRepository bookRepo;

    @PostMapping("/add")
    public String borrow(@RequestParam Long bookId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        Book book = bookRepo.findById(bookId).orElse(null);
        if (book != null && book.getQuantity() > 0) {
            book.setQuantity(book.getQuantity() - 1);
            Borrow b = new Borrow();
            b.setUser(user);
            b.setBook(book);
            b.setBorrowDate(LocalDate.now());
            b.setReturned(false);
            borrowRepo.save(b);
            bookRepo.save(book);
        }
        return "redirect:/user/dashboard";
    }

    @PostMapping("/return/{id}")
    public String returnBook(@PathVariable Long id) {
        Borrow b = borrowRepo.findById(id).orElse(null);
        if (b != null && !b.isReturned()) {
            b.setReturnDate(LocalDate.now());
            b.setReturned(true);
            Book book = b.getBook();
            book.setQuantity(book.getQuantity() + 1);
            bookRepo.save(book);
            borrowRepo.save(b);
        }
        return "redirect:/user/dashboard";
    }
}
