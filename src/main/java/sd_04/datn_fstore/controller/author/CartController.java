package sd_04.datn_fstore.controller.author;

import sd_04.datn_fstore.model.GioHang;
import sd_04.datn_fstore.service.GioHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private GioHangService gioHangService;

    // Hàm này giả định Khách hàng ID là 1.
    private final Integer ID_KHACH_HANG = 1;

    /**
     * 1. Hiển thị trang giỏ hàng (URL: /cart)
     */
    @GetMapping
    public String viewCart(Model model) {

        List<GioHang> cartItems = gioHangService.findByKhachHangId(ID_KHACH_HANG);
        model.addAttribute("cartItems", cartItems);

        // BẮT BUỘC: Tính tổng tiền để hiển thị trong phần tóm tắt đơn hàng
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (GioHang item : cartItems) {
            // Đảm bảo item.getTongTien() đã được set trong Service khi thêm/cập nhật
            if (item.getTongTien() != null) {
                totalPrice = totalPrice.add(item.getTongTien());
            }
        }
        model.addAttribute("totalPrice", totalPrice); // TRUYỀN BIẾN totalPrice

        return "view/author/cart";
    }

    /**
     * 2. Xóa sản phẩm khỏi giỏ hàng (URL: /cart/xoa/{id})
     */
    @GetMapping("/xoa/{id}")
    public String xoaSanPhamTrongGioHang(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            GioHang item = gioHangService.findById(id);
            if (item != null) {
                gioHangService.delete(id);
                redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm trong giỏ hàng.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi xóa sản phẩm.");
        }
        return "redirect:/cart";
    }

    /**
     * 3. Cập nhật số lượng sản phẩm trong giỏ hàng (Tăng/Giảm) (URL: /cart/cap-nhat-so-luong/{id})
     * Phương thức này được gọi khi nhấn nút +/- trong file cart.html
     */
    @GetMapping("/cap-nhat-so-luong/{id}")
    public String capNhatSoLuong(
            @PathVariable("id") Integer id,
            @RequestParam("soLuong") Integer soLuong,
            RedirectAttributes redirectAttributes) {

        if (soLuong == null || soLuong < 1) {
            // Nếu người dùng cố gắng giảm số lượng xuống 0, chuyển hướng sang xóa
            return "redirect:/cart/xoa/" + id;
        }

        try {
            GioHang item = gioHangService.findById(id);
            if (item != null) {
                // Cần kiểm tra tồn kho tại đây
                // if (soLuong > item.getSanPhamChiTiet().getSoLuong()) { ... }

                item.setSoLuong(soLuong);

                // Giả định GiaTien nằm trong SanPhamChiTiet
                BigDecimal giaTien = item.getSanPhamChiTiet().getGiaTien();
                item.setTongTien(giaTien.multiply(new BigDecimal(soLuong)));

                gioHangService.save(item);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật số lượng thành công!");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm trong giỏ hàng.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    /**
     * 4. THÊM sản phẩm vào giỏ hàng (URL: /cart/add)
     * Phương thức này được gọi từ trang chi tiết sản phẩm.
     */
    @PostMapping("/add")
    public String themSanPhamVaoGioHang(
            @RequestParam("idSpCt") Integer idSpCt, // ID SanPhamChiTiet
            @RequestParam("soLuong") Integer soLuong,
            RedirectAttributes redirectAttributes) {

        try {
            if (soLuong == null || soLuong < 1) {
                redirectAttributes.addFlashAttribute("errorMessage", "Số lượng không hợp lệ.");
                return "redirect:/san-pham-chi-tiet/" + idSpCt;
            }

            // 🔥 ĐÃ BỎ CHÚ THÍCH: THỰC HIỆN LOGIC THÊM/CẬP NHẬT TRONG SERVICE
            gioHangService.themHoacCapNhat(ID_KHACH_HANG, idSpCt, soLuong);

            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng thành công!");
        } catch (Exception e) {
            // Ghi log lỗi để dễ dàng debug
            System.err.println("Lỗi khi thêm sản phẩm vào giỏ hàng: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi thêm sản phẩm: " + e.getMessage());
        }

        return "redirect:/cart";
    }
}