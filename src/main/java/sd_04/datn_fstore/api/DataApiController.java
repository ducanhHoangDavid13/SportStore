package sd_04.datn_fstore.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sd_04.datn_fstore.model.*;
import sd_04.datn_fstore.service.*;
// Cần import các Service mới
import sd_04.datn_fstore.service.ChatLieuService;
import sd_04.datn_fstore.service.KichThuocService;
import sd_04.datn_fstore.service.MauSacService;
import sd_04.datn_fstore.service.PhanLoaiService;
import sd_04.datn_fstore.service.TheLoaiService;
import sd_04.datn_fstore.service.XuatXuService;

import java.util.List;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataApiController {

    private final SanPhamCTService sanPhamCTService;
    private final PhieuGiamgiaService phieuGiamgiaService;

    // Các Service cho Thuộc tính mới
    private final PhanLoaiService phanLoaiService;
    private final TheLoaiService theLoaiService;
    private final XuatXuService xuatXuService;
    private final MauSacService mauSacService;
    private final KichThuocService kichThuocService;
    private final ChatLieuService chatLieuService;


    /**
     * API lấy sản phẩm chi tiết đang hoạt động.
     * URL: /api/data/san-pham-chi-tiet-active
     */
    @GetMapping("/san-pham-chi-tiet-active")
    public ResponseEntity<List<SanPhamChiTiet>> getActiveProductDetails() {
        return ResponseEntity.ok(sanPhamCTService.getAllActive());
    }

    /**
     * API lấy phiếu giảm giá đang hoạt động.
     * URL: /api/data/phieu-giam-gia-active
     */
    @GetMapping("/phieu-giam-gia-active")
    public ResponseEntity<List<PhieuGiamGia>> getActiveCoupons() {
        List<PhieuGiamGia> coupons = phieuGiamgiaService.getActive();
        return ResponseEntity.ok(coupons);
    }

    // --- CÁC API MỚI CHO THUỘC TÍNH SẢN PHẨM ---

    /**
     * API lấy danh sách Phân loại đang hoạt động (Category/Department).
     * URL: /api/data/phan-loai-active
     */
    @GetMapping("/phan-loai-active")
    public ResponseEntity<List<PhanLoai>> getActivePhanLoai() {
        // Giả định PhanLoaiService có phương thức getAllActive()
        return ResponseEntity.ok(phanLoaiService.getAllActive());
    }

    /**
     * API lấy danh sách Thể loại đang hoạt động (Tag/Genre).
     * URL: /api/data/the-loai-active
     */
    @GetMapping("/the-loai-active")
    public ResponseEntity<List<TheLoai>> getActiveTheLoai() {
        // Giả định TheLoaiService có phương thức getAllActive()
        return ResponseEntity.ok(theLoaiService.getAllActive());
    }

    /**
     * API lấy danh sách Xuất xứ đang hoạt động (Origin/Country).
     * URL: /api/data/xuat-xu-active
     */
    @GetMapping("/xuat-xu-active")
    public ResponseEntity<List<XuatXu>> getActiveXuatXu() {
        // Giả định XuatXuService có phương thức getAllActive()
        return ResponseEntity.ok(xuatXuService.getAllActive());
    }

    /**
     * API lấy danh sách Màu sắc đang hoạt động (Color).
     * URL: /api/data/mau-sac-active
     */
    @GetMapping("/mau-sac-active")
    public ResponseEntity<List<MauSac>> getActiveMauSac() {
        // Giả định MauSacService có phương thức getAllActive()
        return ResponseEntity.ok(mauSacService.getAllActive());
    }

    /**
     * API lấy danh sách Kích thước đang hoạt động (Size).
     * URL: /api/data/kich-thuoc-active
     */
    @GetMapping("/kich-thuoc-active")
    public ResponseEntity<List<KichThuoc>> getActiveKichThuoc() {
        // Giả định KichThuocService có phương thức getAllActive()
        return ResponseEntity.ok(kichThuocService.getAllActive());
    }

    /**
     * API lấy danh sách Chất liệu đang hoạt động (Material).
     * URL: /api/data/chat-lieu-active
     */
    @GetMapping("/chat-lieu-active")
    public ResponseEntity<List<ChatLieu>> getActiveChatLieu() {
        // Giả định ChatLieuService có phương thức getAllActive()
        return ResponseEntity.ok(chatLieuService.getAllActive());
    }
}