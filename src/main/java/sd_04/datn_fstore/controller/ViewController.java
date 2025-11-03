package sd_04.datn_fstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sd_04.datn_fstore.services.nhanVienServices;

@Controller
public class ViewController {

    @Autowired
    private nhanVienServices nvsv;

    @GetMapping("/nhanvien/view")
    public String viewNhanVien(Model model) {
        model.addAttribute("listNhanVien", nvsv.getAll());
        return "view/nhanVienView"; // đường dẫn file trong /templates/view/
    }
}
