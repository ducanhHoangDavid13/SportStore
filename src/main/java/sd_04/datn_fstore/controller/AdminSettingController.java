package sd_04.datn_fstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminSettingController {

    /**
     * 1. Điều hướng đến trang Cài đặt
     * URL: http://localhost:8080/admin/settings
     */
    @GetMapping("/settings")
    public String viewSettings(Model model) {
        // Tại đây, sau này bạn có thể gọi Service để lấy dữ liệu cấu hình cũ từ DB đổ ra View
        // Ví dụ: model.addAttribute("config", configService.getConfig());

        // Trả về tên file HTML (Lưu ý đường dẫn folder)
        // Nếu file html nằm trong folder: templates/admin/caiDat.html
        return "admin/caiDat";
    }

    /**
     * 2. Xử lý khi bấm nút "Lưu Cấu Hình" (Form Submit)
     */
    @PostMapping("/settings")
    public String saveSettings(
            @RequestParam(value = "siteName", required = false) String siteName,
            @RequestParam(value = "hotline", required = false) String hotline,
            @RequestParam(value = "contactEmail", required = false) String contactEmail,
            @RequestParam(value = "maintenanceMode", required = false) String maintenanceMode,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // --- LOGIC XỬ LÝ (Gọi Service) ---
            // 1. Lưu thông tin text vào DB
            // 2. Lưu file ảnh (logoFile) vào thư mục uploads

            System.out.println("Tên shop: " + siteName);
            System.out.println("Bảo trì: " + (maintenanceMode != null ? "Bật" : "Tắt"));

            // --- THÔNG BÁO THÀNH CÔNG ---
            // Toastr bên Frontend sẽ hiển thị cái này nếu bạn truyền qua model,
            // hoặc dùng ajax như trong file HTML tôi viết thì không cần RedirectAttributes này.
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật cấu hình thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        // Load lại trang cài đặt
        return "redirect:/admin/settings";
    }
}