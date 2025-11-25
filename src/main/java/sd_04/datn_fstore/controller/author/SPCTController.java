
package sd_04.datn_fstore.controller.author;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import sd_04.datn_fstore.model.SanPham;
import sd_04.datn_fstore.repository.SanPhamRepository;

@Controller
public class SPCTController {


    @Autowired
    private SanPhamRepository sanPhamRepository;

    // Mapping theo URL đầy đủ: /san-pham/chi-tiet/{id}
    @GetMapping("/san-pham/chi-tiet/{id}")
    public String chiTiet(@PathVariable Integer id, Model model) {

        // Lấy sản phẩm theo id, nếu không tồn tại trả về null
        SanPham sp = sanPhamRepository.findById(id).orElse(null);

        if (sp == null) {
            // Chuyển hướng về trang danh sách sản phẩm với thông báo lỗi
            return "redirect:/san-pham?error=not_found";
        }

        // Thêm sản phẩm vào model, đặt tên là "product" để dùng trong HTML
        model.addAttribute("product", sp);

        // Trả về file HTML hiển thị chi tiết sản phẩm
        return "view/author/sanPhamChiTiet";
    }


}

