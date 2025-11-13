package sd_04.datn_fstore.controller; // (Hoặc package controller của bạn)

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication; // Cần Spring Security
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import sd_04.datn_fstore.model.NhanVien;
import sd_04.datn_fstore.service.NhanVienService; // (Service bạn đã có)

import java.util.Optional;

@Controller
@RequestMapping("/admin/ban-hang") // Đường dẫn để truy cập trang POS
@RequiredArgsConstructor // (Dùng cái này HOẶC @Autowired, không dùng cả hai)
public class BanHangController {

    private final NhanVienService nhanVienService;

    /**
     * Phương thức này trả về trang HTML/Thymeleaf (banHang.html)
     * để giao diện POS có thể hoạt động.
     */
    @GetMapping
    public String viewBanHangPage(Model model, Authentication authentication) {

        // 1. Kiểm tra xem người dùng đã đăng nhập chưa
        if (authentication == null) {
            // Nếu chưa, chuyển hướng về trang login
            return "redirect:/login";
        }

        // 2. Lấy username (ví dụ: email) của người đang đăng nhập
        String username = authentication.getName();

        // 3. Gọi Service để tìm NhanVien tương ứng
        // (LƯU Ý: Bước này yêu cầu bạn phải thêm hàm findByEmail vào Service)
        Optional<NhanVien> nhanVienOpt = nhanVienService.findByEmail(username);

        if (nhanVienOpt.isPresent()) {
            // 4. Gửi thông tin nhân viên tới file HTML
            model.addAttribute("nhanVienHienTai", nhanVienOpt.get());
        } else {
            // Xử lý nếu không tìm thấy nhân viên (ví dụ: tài khoản admin)
            model.addAttribute("errorMessage", "Lỗi: Không tìm thấy thông tin nhân viên.");
            // Bạn có thể trả về một trang lỗi chung
            return "error/404";
        }

        // 5. Trả về đường dẫn tới file banHang.html của bạn
        // (Khớp với cấu trúc file của bạn: templates/view/admin/banHang.html)
        return "view/admin/banHang";
    }
}



