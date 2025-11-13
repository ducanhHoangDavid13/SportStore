package sd_04.datn_fstore.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sd_04.datn_fstore.model.DiaChi;
import sd_04.datn_fstore.service.DiaChiService;

import java.util.List;

@RestController
@RequestMapping("/api/dia-chi")
public class DiaChiApi {
    @Autowired
    private DiaChiService diaChiService;

    // API: GET /api/dia-chi/khach-hang/{idKhachHang}
    @GetMapping("/khach-hang/{idKhachHang}")
    public List<DiaChi> getDiaChiByKhachHangId(@PathVariable Integer idKhachHang) {
        return diaChiService.getDiaChiByKhachHangId(idKhachHang);
    }
}
