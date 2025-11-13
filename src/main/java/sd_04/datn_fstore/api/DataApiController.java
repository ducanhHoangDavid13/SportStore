package sd_04.datn_fstore.api; // (Tạo trong package api)

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sd_04.datn_fstore.model.PhieuGiamGia;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.service.PhieuGiamgiaService;
import sd_04.datn_fstore.service.SanPhamCTService;

import java.util.List;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataApiController {

    private final SanPhamCTService sanPhamCTService;
    private final PhieuGiamgiaService phieuGiamgiaService;

    /**
     * API này khớp với JS: fetch('/api/data/san-pham-active')
     */
    @GetMapping("/san-pham-active")
    public ResponseEntity<List<SanPhamChiTiet>> getAvailableProducts() {
        // (Bạn phải tạo hàm getAvailableProducts() trong Service/Repo)
        List<SanPhamChiTiet> products = sanPhamCTService.getAvailableProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * API này khớp với JS: fetch('/api/data/phieu-giam-gia-active')
     */
    @GetMapping("/phieu-giam-gia-active")
    public ResponseEntity<List<PhieuGiamGia>> getActiveCoupons() {
        // (Bạn phải tạo hàm getActive() trong Service/Repo)
        List<PhieuGiamGia> coupons = phieuGiamgiaService.getActive();
        return ResponseEntity.ok(coupons);
    }
}