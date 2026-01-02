package sd_04.datn_fstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sd_04.datn_fstore.service.NhanVienService;

@Controller
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class NhanVienViewController {

    @Autowired
    private NhanVienService nhanVienService;

    @GetMapping("/nhanvien/view")
    public String viewNhanVien(Model model) {
        model.addAttribute("listNhanVien", nhanVienService.getAll());
        // ✅ Khớp với file: src/main/resources/templates/view/admin/NhanVienView.html
        return "view/admin/NhanVienView";
    }
}
