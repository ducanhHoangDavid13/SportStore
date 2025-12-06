package sd_04.datn_fstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import sd_04.datn_fstore.service.HoaDonService;

@Controller
@RequestMapping("/admin/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonService hoaDonService;

    // (Giữ nguyên hàm hienThiTrangHoaDon)
    @GetMapping("")
    public String hienThiTrangHoaDon() {
        return "view/admin/hoaDonView";
    }
}