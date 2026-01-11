package sd_04.datn_fstore.controller.author;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sd_04.datn_fstore.dto.GioHangDTO;
import sd_04.datn_fstore.model.GioHang;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.repository.GioHangRepository;
import sd_04.datn_fstore.repository.KhachHangRepo;
import sd_04.datn_fstore.repository.SanPhamCTRepository;
import sd_04.datn_fstore.service.VnPayService;

import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckOutController {

    private final GioHangRepository gioHangRepository;
    private final SanPhamCTRepository sanPhamCTRepository;
    private final KhachHangRepo khachHangRepository; // Thêm để tìm khách hàng
    private final VnPayService vnPayService;

    /**
     * LẤY ID KHÁCH HÀNG THỰC TẾ TỪ SPRING SECURITY
     * (Giống hệt bên CartController để đảm bảo đồng bộ)
     */
    private Integer getCurrentCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        String username = auth.getName();
        Optional<KhachHang> kh = khachHangRepository.findByEmail(username);
        return kh.map(KhachHang::getId).orElse(null);
    }

    // 1. Hiển thị trang HTML Checkout
    @GetMapping
    public String viewCheckoutPage(Model model) {
        if (getCurrentCustomerId() == null) {
            return "redirect:/login";
        }
        return "view/author/checkout";
    }

    // 2. API lấy danh sách sản phẩm đã chọn
    @GetMapping("/api/items")
    @ResponseBody
    public ResponseEntity<?> getSelectedCartItems(
            @RequestParam(value = "cartIds", required = false) List<Integer> cartIds
    ) {
        // Thay đổi: Lấy ID từ Security, không dùng gán cứng
        Integer idKhachHang = getCurrentCustomerId();

        if (idKhachHang == null) return ResponseEntity.status(401).build();

        if (cartIds == null || cartIds.isEmpty()) return ResponseEntity.badRequest().body("No items selected");

        List<GioHangDTO> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // Bảo mật: Chỉ lấy những sản phẩm thuộc về idKhachHang này
        List<GioHang> selectedItems = gioHangRepository.findAllById(cartIds);

        for (GioHang gh : selectedItems) {
            // Kiểm tra: Nếu món đồ trong giỏ không thuộc về người đang đăng nhập thì bỏ qua
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

    // 3. API XÓA GIỎ HÀNG (Giữ nguyên logic xóa nhưng có thể thêm check bảo mật nếu cần)
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
                .id(cartId)
                .idSanPhamChiTiet(spct.getId())
                .tenSanPham(spct.getSanPham().getTenSanPham())
                .tenMau(spct.getMauSac().getTenMauSac())
                .tenKichCo(spct.getKichThuoc().getTenKichThuoc())
                .donGia(spct.getGiaTien())
                .soLuong(qty)
                .tenHinhAnh(spct.getSanPham().getHinhAnh().isEmpty() ? "no-image.png" : spct.getSanPham().getHinhAnh().get(0).getTenHinhAnh())
                .build();
    }
    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        // 1. Lấy toàn bộ tham số từ VNPay trả về
        Map<String, String> vnpParams = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                vnpParams.put(fieldName, fieldValue);
            }
        }

        // 2. Gọi Service để xử lý (Validate hash + Update trạng thái + Hoàn kho nếu lỗi)
        // Hàm này sẽ trả về: 1 (Thành công), 0 (Lỗi/Hủy), -1 (Sai checksum)
        int result = vnPayService.orderReturn(vnpParams);

        String orderId = request.getParameter("vnp_TxnRef");

        // 3. Điều hướng kết quả
        if (result == 1) {
            // Thành công -> Trang cảm ơn
            return "redirect:/checkout/success?id=" + orderId; // Hoặc trang success của bạn
        } else {
            // Thất bại/Hủy -> Trang lỗi
            return "redirect:/checkout/fail?id=" + orderId;    // Hoặc về trang chủ/giỏ hàng
        }
    }
}