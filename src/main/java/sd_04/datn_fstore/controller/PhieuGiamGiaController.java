package sd_04.datn_fstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/phieu-giam-gia")
public class PhieuGiamGiaController {
    @GetMapping("/hien-thi")
    public String hienThi() {
        return "view/admin/PhieuGiamGia";

    }
}
