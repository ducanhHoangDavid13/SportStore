package sd_04.datn_fstore.controller.author;

import jakarta.transaction.Transactional; // ⬅️ CẦN THIẾT cho Lazy Loading
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import sd_04.datn_fstore.model.HinhAnh; // ⬅️ Thêm import cho HinhAnh
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.repository.SanPhamRepository;
import java.util.List;
import java.util.Optional;

@Controller
public class SPCTController {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @GetMapping("/san-pham/chi-tiet/{id}")
    @Transactional // 🟢 Đảm bảo Hibernate Session còn mở để load List<HinhAnh> (Lazy)
    public String chiTiet(@PathVariable Integer id, Model model) {

        Optional<SanPham> optionalSp = sanPhamRepository.findById(id);

        if (optionalSp.isEmpty()) {
            return "redirect:/san-pham?error=not_found";
        }

        SanPham sp = optionalSp.get();

        // 🛠️ BƯỚC KHẮC PHỤC: Lấy và gán tên ảnh chính
        List<HinhAnh> hinhAnhList = sp.getHinhAnh();

        if (hinhAnhList != null && !hinhAnhList.isEmpty()) {
            // Lấy tên file ảnh đầu tiên (hoặc ảnh chính nếu có trường isPrimary)
            String tenFileAnh = hinhAnhList.get(0).getTenHinhAnh();

            // Gán giá trị vào trường @Transient để Thymeleaf có thể sử dụng
            sp.setTenHinhAnhChinh(tenFileAnh);
        } else {
            // ⚠️ Gán ảnh mặc định nếu không có ảnh nào trong DB
            sp.setTenHinhAnhChinh("default_no_image.png");
        }

        model.addAttribute("product", sp);

        // Trả về file HTML hiển thị chi tiết sản phẩm
        return "view/author/sanPhamChiTiet";
    }
}