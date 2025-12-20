package sd_04.datn_fstore.controller.author;


import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sd_04.datn_fstore.dto.GioHangDTO;
import sd_04.datn_fstore.model.GioHang;
import sd_04.datn_fstore.repository.GioHangRepository;
import sd_04.datn_fstore.repository.SanPhamCTRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckOutController {

    private final GioHangRepository gioHangRepository;
    private final SanPhamCTRepository sanPhamCTRepository;

    private Integer getCurrentCustomerId(HttpSession session) {
        return 1; // TEST
    }

    @GetMapping
    public String viewCheckoutPage(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer spctId,
            @RequestParam(required = false) Integer quantity,
            @RequestParam(required = false) List<Integer> cartIds, // Thêm dòng này để nhận danh sách ID
            Model model,
            HttpSession session
    ) {
        Integer idKhachHang = getCurrentCustomerId(session);
        if (idKhachHang == null) return "redirect:/login";

        List<GioHangDTO> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // 🔥 CASE 1: MUA NGAY (Buy Now)
        if ("buy-now".equals(type) && spctId != null && quantity != null) {
            var spct = sanPhamCTRepository.findById(spctId).orElse(null);
            if (spct != null) {
                GioHangDTO dto = mapToDTO(null, spct, quantity);
                items.add(dto);
                total = total.add(dto.getThanhTien());
            }
        }
        // 🛒 CASE 2: THANH TOÁN CÁC MỤC ĐƯỢC CHỌN (cartIds=40,41)
        else if (cartIds != null && !cartIds.isEmpty()) {
            List<GioHang> selectedItems = gioHangRepository.findAllById(cartIds);
            for (GioHang gh : selectedItems) {
                var spct = sanPhamCTRepository.findById(gh.getIdSanPhamChiTiet()).orElse(null);
                if (spct != null) {
                    GioHangDTO dto = mapToDTO(gh.getId(), spct, gh.getSoLuong());
                    items.add(dto);
                    total = total.add(dto.getThanhTien());
                }
            }
        }
        // 🛍️ CASE 3: THANH TOÁN TOÀN BỘ GIỎ HÀNG (Nếu không chọn gì cụ thể)
        else {
            List<GioHang> gioHangs = gioHangRepository.findByIdKhachHang(idKhachHang);
            for (GioHang gh : gioHangs) {
                var spct = sanPhamCTRepository.findById(gh.getIdSanPhamChiTiet()).orElse(null);
                if (spct != null) {
                    GioHangDTO dto = mapToDTO(gh.getId(), spct, gh.getSoLuong());
                    items.add(dto);
                    total = total.add(dto.getThanhTien());
                }
            }
        }

        model.addAttribute("cartItems", items);
        model.addAttribute("totalPrice", total);

        return "view/author/checkout";
    }

    // Hàm bổ trợ để tránh lặp code (Helper method)
    private GioHangDTO mapToDTO(Integer cartId, sd_04.datn_fstore.model.SanPhamChiTiet spct, Integer qty) {
        return GioHangDTO.builder()
                .id(cartId)
                .idSanPhamChiTiet(spct.getId())
                .tenSanPham(spct.getSanPham().getTenSanPham())
                .tenMau(spct.getMauSac().getTenMauSac())
                .tenKichCo(spct.getKichThuoc().getTenKichThuoc())
                .donGia(spct.getGiaTien())
                .soLuong(qty)
                .tenHinhAnh(spct.getSanPham().getHinhAnh().isEmpty()
                        ? "no-image.png"
                        : spct.getSanPham().getHinhAnh().get(0).getTenHinhAnh())
                .build();
    }


    @GetMapping("/success")
    public String viewSuccessPage(@RequestParam(value = "id", required = false) Integer orderId,
                                  Model model) {
        model.addAttribute("orderId", orderId);
        return "view/author/orders";
    }

}

