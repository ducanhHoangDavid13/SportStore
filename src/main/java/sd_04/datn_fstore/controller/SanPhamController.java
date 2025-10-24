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
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.service.SanPhamService;

@Controller
@RequestMapping("/admin/san-pham")
@RequiredArgsConstructor
public class SanPhamController {

    private final SanPhamService sanPhamService;

    /**
     * Tải trang Quản lý Sản phẩm (Danh sách, Lọc, Tìm kiếm VÀ Modal)
     * Đây là hàm duy nhất trong controller này.
     */
    @GetMapping
    public String hienThiTrang(Model model,
                               @PageableDefault(size = 5) Pageable pageable,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer trangThai) {

        // 1. Lấy dữ liệu cho bảng danh sách (có lọc/tìm kiếm)
        // Dữ liệu này được tải lần đầu khi vào trang
        Page<SanPham> sanPhamPage = sanPhamService.searchAndPaginate(pageable, keyword, trangThai);
        model.addAttribute("sanPhamPage", sanPhamPage);

        // 2. Cung cấp 1 đối tượng rỗng cho modal "Thêm mới"
        model.addAttribute("sanPham", new SanPham());

        // 3. Giữ lại các tham số lọc/tìm kiếm (để điền vào form)
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);

        // 4. Trả về file HTML
        return "view/admin/sanPham";
    }

    // KHÔNG CÓ @PostMapping("/save")
    // KHÔNG CÓ @GetMapping("/delete/{id}")
    // Tất cả các action này sẽ do API Controller xử lý
}