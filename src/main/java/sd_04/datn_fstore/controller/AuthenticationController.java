package sd_04.datn_fstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.service.AuthenticationService;

@Controller
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;


    @GetMapping("/login")
    public String getLoginPage() {
        return "login_page";
    }

    @GetMapping("/registration")
    public String getRegistrationPage(Model model) {
        model.addAttribute("user", new KhachHang());
        return "registration_page";
    }

    @PostMapping("/registration")
    public String registerUser(@ModelAttribute KhachHang user) {
        authenticationService.register(user);
        return "redirect:/login?success";
    }
}
