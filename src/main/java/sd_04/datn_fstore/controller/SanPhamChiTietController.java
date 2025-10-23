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
import sd_04.datn_fstore.service.SanPhamCTService;

// Giả sử bạn đã có các Service cho các thuộc tính (để load dropdown)
import sd_04.datn_fstore.service.SanPhamService;
// import sd_04.datn_fstore.service.MauSacService;
// import sd_04.datn_fstore.service.KichThuocService;
// ... (các service khác)

@Controller
@RequestMapping("/admin/san-pham-chi-tiet")
@RequiredArgsConstructor
public class SanPhamChiTietController {

    private final SanPhamCTService sanPhamCTService;

    // Inject các Service cho dropdown (lọc và modal)
    private final SanPhamService sanPhamService;
    // private final MauSacService mauSacService;
    // private final KichThuocService kichThuocService;
    // ... (tất cả các service thuộc tính)

    /**
     * Tải trang Quản lý SPCT (Danh sách, Lọc VÀ Modal)
     */
    @GetMapping
    public String hienThiTrang(Model model,
                               @PageableDefault(size = 10) Pageable pageable,
                               // Tham số cho form lọc
                               @RequestParam(required = false) Integer idSanPham,
                               @RequestParam(required = false) Integer idMauSac,
                               @RequestParam(required = false) Integer idKichThuoc
    ) {

        // 1. Lấy dữ liệu cho bảng (có lọc)
        Page<SanPhamChiTiet> spctPage = sanPhamCTService.search(
                pageable, idSanPham, idKichThuoc, null, null, null, idMauSac, null, null, null, null
        );
        model.addAttribute("spctPage", spctPage);

        // 2. Đối tượng rỗng cho modal "Thêm mới"
        model.addAttribute("spct", new SanPhamChiTiet());

        // 3. Load data cho các bộ lọc và dropdown trong modal
        loadFormDependencies(model);

        // 4. Giữ lại giá trị lọc
        model.addAttribute("idSanPham", idSanPham);
        model.addAttribute("idMauSac", idMauSac);
        model.addAttribute("idKichThuoc", idKichThuoc);

        // 5. Trả về file: /templates/view/admin/sanPhamCT.html
        return "view/admin/sanPhamCT";
    }

    /**
     * Helper: Tải dữ liệu cho các dropdown
     */
    private void loadFormDependencies(Model model) {
        // model.addAttribute("listSanPham", sanPhamService.getAll());
        // model.addAttribute("listMauSac", mauSacService.getAll());
        // model.addAttribute("listKichThuoc", kichThuocService.getAll());
        // ... (load tất cả các list thuộc tính)
    }

    // KHÔNG CÓ @PostMapping("/save")
    // KHÔNG CÓ @GetMapping("/delete/{id}")
}