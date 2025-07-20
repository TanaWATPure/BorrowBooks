package com.example.demo.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.Repository.BorrowRepository;

@Controller
@RequestMapping("/admin/report")
public class ReportController {
    @Autowired BorrowRepository borrowRepo;

    @GetMapping
    public String report(Model model) {
        model.addAttribute("borrows", borrowRepo.findAll());
        return "report";
    }
}