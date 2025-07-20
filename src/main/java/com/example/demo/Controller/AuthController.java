package com.example.demo.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        System.out.println("Login attempt: username=" + username + ", password=" + password);
        User user = userRepository.findByUsernameAndPassword(username, password);

        if (user != null) {
            System.out.println("Login success for user: " + user.getUsername());
            session.setAttribute("user", user);

            if ("admin".equals(user.getRole())) {
                return "redirect:/admin/dashboard/" + user.getId();
            } else {
                return "redirect:/user/dashboard/" + user.getId();
            }
        } else {
            System.out.println("Login failed: invalid username or password");
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
