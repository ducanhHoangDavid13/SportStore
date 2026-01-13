package sd_04.datn_fstore.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sd_04.datn_fstore.dto.KhachHangRegistration;
import sd_04.datn_fstore.dto.KhachHangRequest;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.service.KhachhangService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthenticationController {

    private final KhachhangService khachHangService;

    @GetMapping("/login")
    public String getLoginPage() {
        return "login_page";
    }

    @GetMapping("/registration-view")
    public String getRegistrationPage(@ModelAttribute("user") KhachHangRequest dto) {
        return "registration_page";
    }

    @PostMapping("/registration")
    public String registration(@ModelAttribute("user") KhachHangRegistration dto,
                               RedirectAttributes redirectAttributes) {
        try {
            khachHangService.save(dto);
            redirectAttributes.addAttribute("success", true);
            return "redirect:/login";
        } catch (RuntimeException ex) {
            redirectAttributes.addAttribute("error", ex.getMessage());
            return "redirect:/registration-view";
        }
    }

}
