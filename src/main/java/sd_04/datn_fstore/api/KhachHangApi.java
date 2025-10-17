package sd_04.datn_fstore.api;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;
import sd_04.datn_fstore.model.KhachHang;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sd_04.datn_fstore.service.KhachhangService;

import java.util.List;

@RestController
@RequestMapping("/api/khach-hang")
public class KhachHangApi {
    @Autowired
    private KhachhangService khachHangService;

    private final int pageSize = 5; // Kích thước trang mặc định

    // API Endpoint: /api/khach-hang
    @GetMapping
    public Page<KhachHang> getKhachHangList(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sdt,
            @RequestParam(required = false) Boolean gioiTinh) {

        return khachHangService.getFilteredKhachHang(keyword, sdt, gioiTinh, pageNo, pageSize);
    }

}
