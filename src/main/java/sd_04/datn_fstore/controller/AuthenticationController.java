package sd_04.datn_fstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sd_04.datn_fstore.model.KhachHang;

@Controller
@RequiredArgsConstructor
public class AuthenticationController {


    @GetMapping("/login")
    public String getLoginPage() {
        return "login_page";
    }

    @GetMapping("/registration")
    public String getRegistrationPage(Model model) {
        model.addAttribute("user", new KhachHang());
        return "registration_page";
    }
}
