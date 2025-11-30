package sd_04.datn_fstore.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sd_04.datn_fstore.dto.AdminSettingDto;

import java.io.IOException;
import java.nio.file.*;

@Controller
@RequestMapping("/admin")
public class AdminSettingController {

    // Đường dẫn lưu ảnh (nên cấu hình trong application.properties, nhưng demo để đây)
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @GetMapping("/settings")
    public String viewSettings(Model model) {
        // GIẢ LẬP: Lấy dữ liệu từ DB lên để hiển thị
        AdminSettingDto currentSettings = new AdminSettingDto();
        currentSettings.setSiteName("F-Store Fashion");
        currentSettings.setHotline("0988.888.999");
        currentSettings.setMaintenanceMode(false); // Ví dụ đang tắt bảo trì
        currentSettings.setMailHost("smtp.gmail.com");

        // Đẩy object này sang View để Thymeleaf binding dữ liệu
        model.addAttribute("settingDto", currentSettings);

        return "view/admin/caiDat"; // Thêm chữ view/ vào đầu
    }

    @PostMapping("/settings")
    public String saveSettings(
            @ModelAttribute("settingDto") AdminSettingDto settingDto, // Hứng toàn bộ form vào đây
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile, // File xử lý riêng
            RedirectAttributes redirectAttributes
    ) {
        try {
            // 1. Xử lý lưu ảnh (nếu có upload ảnh mới)
            if (logoFile != null && !logoFile.isEmpty()) {
                String fileName = "logo_" + System.currentTimeMillis() + ".png";
                Path uploadPath = Paths.get(UPLOAD_DIR);
                if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

                Files.copy(logoFile.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Đã lưu logo mới: " + fileName);
                // Sau đó bạn set tên file vào DTO hoặc Entity để lưu DB
            }

            // 2. LOGIC LƯU DB (Gọi Service)
            // configService.save(settingDto);

            System.out.println("Tên shop mới: " + settingDto.getSiteName());
            System.out.println("Chế độ bảo trì: " + settingDto.isMaintenanceMode());
            System.out.println("VNPAY Code: " + settingDto.getVnp_TmnCode());

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật cấu hình thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }
}