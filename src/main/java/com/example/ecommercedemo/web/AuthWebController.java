package com.example.ecommercedemo.web;

import com.example.ecommercedemo.services.users.UserService;
import com.example.ecommercedemo.web.forms.RegisterForm;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
public class AuthWebController {

    private final UserService userService;

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        if (request.getParameter("error") != null) {
            model.addAttribute("loginError", "Invalid email or password.");
        }
        if (request.getParameter("logout") != null) {
            model.addAttribute("flash", "You have been signed out.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new RegisterForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerForm") RegisterForm form,
                           BindingResult binding,
                           Model model,
                           RedirectAttributes redirect) {
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            binding.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (form.getEmail() != null && !form.getEmail().isBlank() && userService.emailExists(form.getEmail())) {
            binding.rejectValue("email", "exists", "That email is already registered");
        }
        if (binding.hasErrors()) {
            model.addAttribute("fieldErrors", WebErrors.fieldErrors(binding));
            return "register";
        }
        userService.createUser(form.toCreateUserDTO());
        redirect.addFlashAttribute("flash", "Account created — please sign in.");
        return "redirect:/login";
    }
}
