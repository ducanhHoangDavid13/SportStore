package sd_04.datn_fstore.controller;

import jakarta.servlet.http.HttpServletResponse;
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
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.service.SanPhamService;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/admin/san-pham")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_ADMIN')")
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
    // === PHẦN SỬA ĐỔI BẮT ĐẦU TỪ ĐÂY ===
//    @GetMapping("/api/san-pham/export/excel")
//    public void exportToExcel(
//            @RequestParam(value = "keyword", required = false) String keyword,
//            @RequestParam(value = "trangThai", required = false) Integer trangThai,
//            HttpServletResponse response) throws IOException {
//
//        // 1. Thiết lập Header cho HTTP Response (Đã có)
//        response.setContentType("application/octet-stream"); // Kiểu file
//        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//        String currentDateTime = dateFormatter.format(new Date());
//
//        String headerKey = "Content-Disposition";
//        String headerValue = "attachment; filename=danh_sach_san_pham_" + currentDateTime + ".xlsx";
//        response.setHeader(headerKey, headerValue); // Trình duyệt sẽ tự động tải về file
//
//        // 2. Lấy danh sách sản phẩm TỪ SERVICE (KHÔNG phân trang)
//        // Bạn sẽ cần tạo phương thức này trong Service (xem Phần 3)
//        List<SanPham> listSanPhams = sanPhamService.findAllForExport(keyword, trangThai);
//
//        // 3. Gọi lớp Exporter để tạo file Excel
//        // Bạn sẽ tạo lớp này (xem Phần 2)
//        SanPhamExcelExporter excelExporter = new SanPhamExcelExporter(listSanPhams);
//        excelExporter.export(response); // Ghi file vào response
//    }
}