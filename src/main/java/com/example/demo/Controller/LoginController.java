package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.Model.User;
import com.example.demo.Service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private UserService userRepo;
    
    // แสดงหน้า Login
    @GetMapping({"/", "/login"})
    public String showLoginPage(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute User user, HttpSession session, Model model) {
        User foundUser = userRepo.findByUsernameAndPassword(user.getUsername(), user.getPassword());
        
        if (foundUser != null) {
            session.setAttribute("user", foundUser);
            if ("admin".equalsIgnoreCase(foundUser.getRole())) {
                return "redirect:/admin/dashboard/" + foundUser.getId();
            } else {
                return "redirect:/user/dashboard/" + foundUser.getId();
            }
        }
        
        model.addAttribute("error", "Invalid username or password");
        return "login";
    }


    // จัดการการ Logout
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/login?logout";
    }
}