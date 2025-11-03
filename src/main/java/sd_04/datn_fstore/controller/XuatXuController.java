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
import sd_04.datn_fstore.model.XuatXu;
import sd_04.datn_fstore.service.XuatXuService;

@Controller
@RequestMapping("/admin/xuat-xu")
@RequiredArgsConstructor
public class XuatXuController {

    private final XuatXuService xuatXuService;

    /**
     * Tải trang Quản lý Xuất Xứ (Danh sách, Lọc, Tìm kiếm VÀ Modal)
     */
    @GetMapping
    public String hienThiTrang(Model model,
                               @PageableDefault(size = 1000) Pageable pageable,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Integer trangThai) {

        // 1. Lấy dữ liệu cho bảng danh sách (có lọc/tìm kiếm và phân trang)
        Page<XuatXu> xuatXuPage = xuatXuService.searchAndPaginate(pageable, keyword, trangThai);
        model.addAttribute("xuatXuPage", xuatXuPage);

        // 2. Cung cấp 1 đối tượng rỗng cho modal "Thêm mới"
        model.addAttribute("xuatXu", new XuatXu());

        // 3. Giữ lại các tham số lọc/tìm kiếm (để điền vào form)
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);

        // 4. Trả về file HTML theo cấu trúc của bạn (view/admin/xuatXu.html)
        return "view/admin/xuatXu";
    }
}