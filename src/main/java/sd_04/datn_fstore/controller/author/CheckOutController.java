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
    public String viewCheckoutPage(Model model, HttpSession session) {

        Integer idKhachHang = getCurrentCustomerId(session);
        if(idKhachHang == null){
            return "redirect:/login";
        }

        // Lấy giỏ hàng
        List<GioHang> gioHangs = gioHangRepository.findByIdKhachHang(idKhachHang);

        // Map sang DTO
        List<GioHangDTO> items = new ArrayList<>();

        BigDecimal total = BigDecimal.ZERO;

        for(GioHang gh : gioHangs){
            var spct = sanPhamCTRepository.findById(gh.getIdSanPhamChiTiet()).orElse(null);
            if(spct == null) continue;

            GioHangDTO dto = GioHangDTO.builder()
                    .id(gh.getId())
                    .idSanPhamChiTiet(spct.getId())
                    .tenSanPham(spct.getSanPham().getTenSanPham())
                    .tenMau(spct.getMauSac().getTenMauSac())
                    .tenKichCo(spct.getKichThuoc().getTenKichThuoc())
                    .donGia(spct.getGiaTien())
                    .soLuong(gh.getSoLuong())
                    .tenHinhAnh(
                            spct.getSanPham().getHinhAnh().isEmpty() ?
                                    "no-image.png" :
                                    spct.getSanPham().getHinhAnh().get(0).getTenHinhAnh()
                    )
                    .build();

            total = total.add(dto.getThanhTien());
            items.add(dto);
        }

        // Gửi sang HTML
        model.addAttribute("cartItems", items);
        model.addAttribute("totalPrice", total);

        return "view/author/checkout";
    }

    @GetMapping("/success")
    public String viewSuccessPage(@RequestParam(value = "id", required = false) Integer orderId,
                                  Model model) {
        model.addAttribute("orderId", orderId);
        return "view/author/orders";
    }
}

