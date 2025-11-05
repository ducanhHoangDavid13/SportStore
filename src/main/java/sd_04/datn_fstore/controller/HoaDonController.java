package sd_04.datn_fstore.controller; // Gói controller admin của bạn

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sd_04.datn_fstore.model.HoaDon;
import sd_04.datn_fstore.service.HoaDonService; // Dùng Service Admin

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/hoa-don") // Đường dẫn cho trang Admin
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonService hoaDonService;

    /**
     * Endpoint để hiển thị trang Danh sách Hóa đơn (phân trang, lọc)
     */
    @GetMapping("")
    public String hienThiDanhSach(Model model,
                                  @PageableDefault(size = 10) Pageable pageable,
                                  @RequestParam(required = false) List<Integer> trangThaiList,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayBatDau,
                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ngayKetThuc,
                                  @RequestParam(required = false) String keyword) {

        // Gọi hàm search từ HoaDonService
        Page<HoaDon> hoaDonPage = hoaDonService.search(
                pageable, trangThaiList, ngayBatDau, ngayKetThuc, keyword
        );

        model.addAttribute("hoaDonPage", hoaDonPage);
        // Lưu lại các bộ lọc để hiển thị trên input
        model.addAttribute("trangThaiList", trangThaiList);
        model.addAttribute("ngayBatDau", ngayBatDau);
        model.addAttribute("ngayKetThuc", ngayKetThuc);
        model.addAttribute("keyword", keyword);

        // Trả về tên file Thymeleaf (Khớp với cấu trúc file của bạn)
        return "view/admin/hoaDonView";
    }

    /**
     * Endpoint để xử lý các nút bấm "Xác nhận", "Giao hàng", "Hủy"
     */
    @PostMapping("/update-status")
    public String updateStatus(
            @RequestParam("hoaDonId") Integer hoaDonId,
            @RequestParam("newTrangThai") Integer newTrangThai,
            RedirectAttributes redirectAttributes) {

        try {
            // Gọi hàm nghiệp vụ (đã bao gồm logic hoàn kho)
            hoaDonService.updateTrangThai(hoaDonId, newTrangThai);
            redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi: " + e.getMessage());
        }

        // Quay trở lại trang danh sách hóa đơn
        return "redirect:/admin/hoa-don";
    }
}