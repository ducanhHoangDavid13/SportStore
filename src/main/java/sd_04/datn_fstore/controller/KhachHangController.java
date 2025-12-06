package sd_04.datn_fstore.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/khach-hang")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class KhachHangController {
    @GetMapping("/hien-thi")
    public String hienThi() {
        return "view/admin/khachhang"; // Trả về file HTML t Thymeleaf
    }




}
