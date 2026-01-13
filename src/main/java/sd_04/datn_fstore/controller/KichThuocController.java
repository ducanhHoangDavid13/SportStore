package sd_04.datn_fstore.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sd_04.datn_fstore.model.KichThuoc;
import sd_04.datn_fstore.service.KichThuocService;

@Controller
@RequestMapping("/admin/kich-thuoc")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class KichThuocController {

    private final KichThuocService kichThuocService;

    /**
     * Tải trang Quản lý Kích Thước (Danh sách, Lọc, Tìm kiếm VÀ Modal)
     */
    @GetMapping
    public String hienThiTrang(Model model,
                               @PageableDefault(size = 1000) Pageable pageable,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer trangThai) {

        // 1. Lấy dữ liệu cho bảng danh sách (có lọc/tìm kiếm và phân trang)
        Page<KichThuoc> kichThuocPage = kichThuocService.searchAndPaginate(pageable, keyword, trangThai);
        model.addAttribute("kichThuocPage", kichThuocPage);

        // 2. Cung cấp 1 đối tượng rỗng cho modal "Thêm mới"
        model.addAttribute("kichThuoc", new KichThuoc());

        // 3. Giữ lại các tham số lọc/tìm kiếm (để điền vào form)
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);

        // 4. Trả về file HTML theo cấu trúc của bạn (view/admin/kichThuoc.html)
        return "view/admin/kickThuoc";
    }
}