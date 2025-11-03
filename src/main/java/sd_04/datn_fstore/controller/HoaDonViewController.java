package sd_04.datn_fstore.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sd_04.datn_fstore.services.HoaDonService;

@Controller
public class HoaDonViewController {

    @Autowired
    private HoaDonService hoaDonService;

    @GetMapping("/hoadon/view")
    public String viewHoaDon(Model model) {
        model.addAttribute("listHoaDon", hoaDonService.getAll());
        return "view/hoaDonView"; // file: src/main/resources/templates/view/hoaDonView.html
    }
}
