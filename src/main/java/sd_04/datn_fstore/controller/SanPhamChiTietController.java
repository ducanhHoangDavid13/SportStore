package sd_04.datn_fstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sd_04.datn_fstore.model.SanPhamChiTiet;

// Import các service
import sd_04.datn_fstore.service.*; // Import tất cả service cho gọn

import java.math.BigDecimal; // Import kiểu BigDecimal cho giá

@Controller
@RequestMapping("/admin/san-pham-chi-tiet")
@RequiredArgsConstructor
public class SanPhamChiTietController {

    private final SanPhamCTService sanPhamCTService;

    // Inject các Service cho dropdown (lọc và modal)
    private final SanPhamService sanPhamService;
    private final MauSacService mauSacService;
    private final KichThuocService kichThuocService;
    private final ChatLieuService chatLieuService;
    private final TheLoaiService theLoaiService;
    private final XuatXuService xuatXuService;
    private final PhanLoaiService phanLoaiService;

    /**
     * Tải trang Quản lý SPCT (Danh sách, Lọc VÀ Modal)
     */
    @GetMapping
    public String hienThiTrang(Model model,
                               @PageableDefault(size = 1000) Pageable pageable,

                               // --- SỬA 1: Bổ sung đầy đủ các tham số lọc ---
                               @RequestParam(required = false) Integer idSanPham,
                               @RequestParam(required = false) Integer idMauSac,
                               @RequestParam(required = false) Integer idKichThuoc,
                               @RequestParam(required = false) Integer idChatLieu,
                               @RequestParam(required = false) Integer idTheLoai,
                               @RequestParam(required = false) Integer idXuatXu,
                               @RequestParam(required = false) Integer idPhanLoai,
                               @RequestParam(required = false) Integer trangThai,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) BigDecimal minPrice, // Dùng BigDecimal cho giá
                               @RequestParam(required = false) BigDecimal maxPrice  // Dùng BigDecimal cho giá
    ) {

        // --- SỬA 2: Sắp xếp lại thứ tự tham số khi gọi Service ---
        // (Giả định thứ tự trong Service là: IDs, Prices, Status, Keyword)
        Page<SanPhamChiTiet> spctPage = sanPhamCTService.search(
                pageable,
                idSanPham, idKichThuoc, idChatLieu,
                idTheLoai, idXuatXu, idMauSac, idPhanLoai,
                minPrice, maxPrice, // Giá (BigDecimal) phải được đặt trước
                trangThai,          // Trạng thái (Integer)
                keyword             // Keyword (String)
        );
        model.addAttribute("spctPage", spctPage);

        // 2. Đối tượng rỗng cho modal "Thêm mới"
        model.addAttribute("spct", new SanPhamChiTiet());

        // 3. Load data cho các bộ lọc và dropdown trong modal
        loadFormDependencies(model);

        // 4. Giữ lại giá trị lọc
        model.addAttribute("idSanPham", idSanPham);
        model.addAttribute("idMauSac", idMauSac);
        // ... (Bạn có thể thêm các thuộc tính khác nếu cần)

        // 5. Trả về file: /templates/view/admin/sanPhamCT.html
        return "view/admin/sanPhamCT";
    }

    /**
     * Helper: Tải dữ liệu cho các dropdown
     */
    private void loadFormDependencies(Model model) {
        model.addAttribute("listSanPham", sanPhamService.getAll());
        model.addAttribute("listMauSac", mauSacService.getAll());
        model.addAttribute("listKichThuoc", kichThuocService.getAll());
        model.addAttribute("listChatLieu", chatLieuService.getAll());
        model.addAttribute("listTheLoai", theLoaiService.getAll());
        model.addAttribute("listXuatXu", xuatXuService.getAll());
        model.addAttribute("listPhanLoai", phanLoaiService.getAll());
    }

    // KHÔNG CÓ @PostMapping("/save")
    // KHÔNG CÓ @GetMapping("/delete/{id}")
}