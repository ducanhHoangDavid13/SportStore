package sd_04.datn_fstore.controller.author;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.GioHangDTO;
import sd_04.datn_fstore.model.GioHang;
import sd_04.datn_fstore.repository.GioHangRepository;
import sd_04.datn_fstore.repository.SanPhamCTRepository;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckOutController {

    private final GioHangRepository gioHangRepository;
    private final SanPhamCTRepository sanPhamCTRepository;

    private Integer getCurrentCustomerId(HttpSession session) {
        return 1; // TEST: Thay bằng logic lấy user thực tế của bạn
    }

    // 1. Hiển thị trang HTML Checkout
    @GetMapping
    public String viewCheckoutPage(Model model, HttpSession session) {
        return "view/author/checkout";
    }

    // 2. API lấy danh sách sản phẩm đã chọn (để trang checkout gọi lúc load trang)
    @GetMapping("/api/items")
    @ResponseBody
    public ResponseEntity<?> getSelectedCartItems(
            @RequestParam(value = "cartIds", required = false) List<Integer> cartIds,
            HttpSession session
    ) {
        Integer idKhachHang = getCurrentCustomerId(session);
        if (idKhachHang == null) return ResponseEntity.status(401).build();

        if (cartIds == null || cartIds.isEmpty()) return ResponseEntity.badRequest().body("No items selected");

        List<GioHangDTO> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        List<GioHang> selectedItems = gioHangRepository.findAllById(cartIds);
        for (GioHang gh : selectedItems) {
            if (gh.getIdKhachHang().equals(idKhachHang)) {
                var spct = sanPhamCTRepository.findById(gh.getIdSanPhamChiTiet()).orElse(null);
                if (spct != null) {
                    GioHangDTO dto = mapToDTO(gh.getId(), spct, gh.getSoLuong());
                    items.add(dto);
                    total = total.add(dto.getThanhTien());
                }
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("cartItems", items);
        response.put("totalPrice", total);
        return ResponseEntity.ok(response);
    }

    // 3. API XÓA GIỎ HÀNG (Dùng sau khi bấm nút thanh toán)
    @PostMapping("/api/clear-cart")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> clearCartAfterCheckout(@RequestBody List<Integer> cartIds) {
        try {
            if (cartIds != null && !cartIds.isEmpty()) {
                gioHangRepository.deleteAllById(cartIds);
                return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa giỏ hàng"));
            }
            return ResponseEntity.badRequest().body("Danh sách xóa trống");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/success")
    public String viewSuccessPage() {
        return "view/author/orders";
    }

    private GioHangDTO mapToDTO(Integer cartId, sd_04.datn_fstore.model.SanPhamChiTiet spct, Integer qty) {
        return GioHangDTO.builder()
                .id(cartId) // Đây là ID của bảng GioHang (Rất quan trọng để xóa)
                .idSanPhamChiTiet(spct.getId())
                .tenSanPham(spct.getSanPham().getTenSanPham())
                .tenMau(spct.getMauSac().getTenMauSac())
                .tenKichCo(spct.getKichThuoc().getTenKichThuoc())
                .donGia(spct.getGiaTien())
                .soLuong(qty)
                .tenHinhAnh(spct.getSanPham().getHinhAnh().isEmpty() ? "no-image.png" : spct.getSanPham().getHinhAnh().get(0).getTenHinhAnh())
                .build();
    }
}