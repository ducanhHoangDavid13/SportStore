package sd_04.datn_fstore.api;

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
     * API lấy sản phẩm đang hoạt động.
     * Luôn trả về số lượng tồn kho thực tế tại thời điểm gọi.
     */
    @GetMapping("/san-pham-chi-tiet-active")
    public ResponseEntity<List<SanPhamChiTiet>> getActiveProductDetails() {
        // SỬA: Gọi hàm không tham số để lấy toàn bộ danh sách
        return ResponseEntity.ok(sanPhamCTService.getAllActive());
    }

    /**
     * API lấy phiếu giảm giá đang hoạt động.
     */
    @GetMapping("/phieu-giam-gia-active")
    public ResponseEntity<List<PhieuGiamGia>> getActiveCoupons() {
        // Đảm bảo Service có hàm getActive() lọc theo ngày bắt đầu/kết thúc và trạng thái
        List<PhieuGiamGia> coupons = phieuGiamgiaService.getActive();
        return ResponseEntity.ok(coupons);
    }
}