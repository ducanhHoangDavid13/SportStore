package sd_04.datn_fstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/khach-hang")
public class KhachHangController {
    @GetMapping("/hien-thi")
    public String hienThi() {
        return "view/khachhang"; // Trả về file HTML t Thymeleaf
    }




}
