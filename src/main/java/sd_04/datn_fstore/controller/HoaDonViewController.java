package sd_04.datn_fstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
<<<<<<< Updated upstream
import sd_04.datn_fstore.services.HoaDonService;
=======
import sd_04.datn_fstore.service.HoaDonService;
>>>>>>> Stashed changes

@Controller
public class HoaDonViewController {

    @Autowired
    private HoaDonService hoaDonService;

    @GetMapping("/hoadon/view")
    public String viewHoaDon(Model model) {
        model.addAttribute("listHoaDon", hoaDonService.getAll());
<<<<<<< Updated upstream
        return "view/hoaDonView"; // file: src/main/resources/templates/view/hoaDonView.html
=======
        // ✅ Đường dẫn này khớp với: src/main/resources/templates/view/admin/HoaDonView.html
        return "view/admin/HoaDonView";
>>>>>>> Stashed changes
    }
}
