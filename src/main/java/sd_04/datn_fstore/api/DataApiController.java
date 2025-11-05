package sd_04.datn_fstore.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.model.PhieuGiamGia;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.repository.PhieuGiamGiaRepo;
import sd_04.datn_fstore.repository.SanPhamCTRepository;

import java.util.List;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DataApiController {

    private final SanPhamCTRepository sanPhamCTRepository;
    private final PhieuGiamGiaRepo phieuGiamGiaRepo;

    /**
     * API để lấy danh sách sản phẩm hiển thị bên phải
     */
    @GetMapping("/san-pham-active")
    public ResponseEntity<?> getActiveProducts() {
        // Lấy tất cả SP có trạng thái = 1 (Đang hoạt động)
        List<SanPhamChiTiet> products = sanPhamCTRepository.findAllByTrangThai(1);
        return ResponseEntity.ok(products);
    }

    /**
     * API để lấy danh sách phiếu giảm giá cho combobox
     */
    @GetMapping("/phieu-giam-gia-active")
    public ResponseEntity<?> getActiveCoupons() {
        // Lấy tất cả PGG có trạng thái = 0 (Đang hoạt động)
        List<PhieuGiamGia> coupons = phieuGiamGiaRepo.findAllByTrangThai(0);
        return ResponseEntity.ok(coupons);
    }
}