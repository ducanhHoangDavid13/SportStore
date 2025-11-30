package sd_04.datn_fstore.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RedirectController {

    @GetMapping("/redirect")
    public String redirectAfterLogin(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        boolean isNhanVien = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYEE"));

        if (isAdmin) {
            return "redirect:/admin/dashboard";  // Admin dashboard
        } else if (isNhanVien) {
            return "redirect:/admin/ban-hang";  // Trang dành cho Nhân viên
        } else {
            return "redirect:/home";             // Các user khác
        }
    }

}
