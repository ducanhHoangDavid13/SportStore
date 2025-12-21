package sd_04.datn_fstore.controller.author;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sd_04.datn_fstore.dto.GioHangDTO;
import sd_04.datn_fstore.model.GioHang;
import sd_04.datn_fstore.model.KhachHang;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.repository.GioHangRepository;
import sd_04.datn_fstore.repository.KhachHangRepo; // Thêm Repository này
import sd_04.datn_fstore.repository.SanPhamCTRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final GioHangRepository gioHangRepository;
    private final SanPhamCTRepository sanPhamCTRepository;
    private final KhachHangRepo khachHangRepository; // Thêm mới để tìm khách hàng

    /**
     * LẤY ID KHÁCH HÀNG ĐANG ĐĂNG NHẬP TỪ SPRING SECURITY
     */
    private Integer getCurrentCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Kiểm tra nếu chưa đăng nhập hoặc là tài khoản ẩn danh
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }

        String username = auth.getName(); // Lấy email hoặc username đăng nhập

        // Tìm khách hàng trong DB dựa trên username
        Optional<KhachHang> kh = khachHangRepository.findByEmail(username); // Giả sử dùng Email đăng nhập
        if (kh.isPresent()) {
            return kh.get().getId();
        }

        return null;
    }

    private List<GioHangDTO> mapToDto(List<GioHang> gioHangs) {
        List<GioHangDTO> dtoList = new ArrayList<>();
        for (GioHang gh : gioHangs) {
            if (gh.getIdSanPhamChiTiet() == null) continue;
            Optional<SanPhamChiTiet> optSpct = sanPhamCTRepository.findById(gh.getIdSanPhamChiTiet());
            if (optSpct.isPresent()) {
                SanPhamChiTiet spct = optSpct.get();

                String tenHinhAnh = "no-image.png";
                if (spct.getSanPham().getHinhAnh() != null && !spct.getSanPham().getHinhAnh().isEmpty()) {
                    tenHinhAnh = spct.getSanPham().getHinhAnh().get(0).getTenHinhAnh();
                }

                GioHangDTO dto = GioHangDTO.builder()
                        .id(gh.getId())
                        .soLuong(gh.getSoLuong() != null ? gh.getSoLuong() : 1)
                        .idSanPhamChiTiet(spct.getId())
                        .donGia(spct.getGiaTien())
                        .idSanPham(spct.getSanPham().getId())
                        .tenSanPham(spct.getSanPham().getTenSanPham())
                        .tenMau(spct.getMauSac().getTenMauSac())
                        .tenKichCo(spct.getKichThuoc().getTenKichThuoc())
                        .tenHinhAnh(tenHinhAnh)
                        .soLuongTon(spct.getSoLuong())
                        .build();
                dtoList.add(dto);
            }
        }
        return dtoList;
    }

    @GetMapping("cart")
    public String index(Model model) {
        Integer idKhachHang = getCurrentCustomerId();

        if (idKhachHang == null) {
            return "redirect:/login"; // Bắt buộc đăng nhập để xem giỏ hàng
        }

        List<GioHang> gioHangs = gioHangRepository.findByIdKhachHang(idKhachHang);
        List<GioHangDTO> cartItems = mapToDto(gioHangs);

        BigDecimal totalPrice = BigDecimal.ZERO;
        int totalItems = 0;
        for (GioHangDTO item : cartItems) {
            totalPrice = totalPrice.add(item.getThanhTien());
            totalItems += item.getSoLuong();
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("totalItems", totalItems);

        return "view/author/cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
                            @RequestParam("soLuong") Integer soLuong,
                            RedirectAttributes ra) {

        Integer idKhachHang = getCurrentCustomerId();
        if (idKhachHang == null) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập để thêm vào giỏ hàng!");
            return "redirect:/login";
        }

        Optional<SanPhamChiTiet> optSpct = sanPhamCTRepository.findById(idSanPhamChiTiet);
        if (optSpct.isEmpty()) {
            ra.addFlashAttribute("error", "Sản phẩm không tồn tại.");
            return "redirect:/";
        }
        SanPhamChiTiet spct = optSpct.get();

        if (spct.getSoLuong() == null || spct.getSoLuong() < soLuong) {
            ra.addFlashAttribute("error", "Số lượng yêu cầu vượt quá tồn kho.");
            return "redirect:/sanpham/chi/tiet/" + idSanPhamChiTiet;
        }

        Optional<GioHang> existingCartItem = gioHangRepository.findByIdKhachHangAndIdSanPhamChiTiet(
                idKhachHang, idSanPhamChiTiet
        );

        try {
            if (existingCartItem.isPresent()) {
                GioHang gh = existingCartItem.get();
                int newQuantity = gh.getSoLuong() + soLuong;
                if (spct.getSoLuong() < newQuantity) {
                    ra.addFlashAttribute("error", "Tổng số lượng vượt quá tồn kho.");
                    return "redirect:/sanpham/chi/tiet/" + idSanPhamChiTiet;
                }
                gh.setSoLuong(newQuantity);
                gh.setNgaySua(LocalDateTime.now());
                gioHangRepository.save(gh);
            } else {
                GioHang newCartItem = new GioHang();
                newCartItem.setIdKhachHang(idKhachHang);
                newCartItem.setIdSanPhamChiTiet(idSanPhamChiTiet);
                newCartItem.setIdSanPham(spct.getSanPham().getId());
                newCartItem.setSoLuong(soLuong);
                newCartItem.setNgayTao(LocalDateTime.now());
                newCartItem.setNgaySua(LocalDateTime.now());
                newCartItem.setTrangThai(1);
                gioHangRepository.save(newCartItem);
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/sanpham/chi/tiet/" + idSanPhamChiTiet;
        }

        ra.addFlashAttribute("success", "Đã thêm vào giỏ hàng!");
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    @ResponseBody
    public String updateCartItem(@RequestParam("id") Integer cartItemId,
                                 @RequestParam("quantityChange") Integer change) {
        Optional<GioHang> optGh = gioHangRepository.findById(cartItemId);
        if (optGh.isEmpty()) return "error: Mục không tồn tại.";

        GioHang gh = optGh.get();
        // Bảo mật: Kiểm tra xem mục giỏ hàng này có đúng của người đang đăng nhập không
        if (!gh.getIdKhachHang().equals(getCurrentCustomerId())) {
            return "error: Bạn không có quyền sửa giỏ hàng này.";
        }

        int newQuantity = gh.getSoLuong() + change;
        if (newQuantity < 1) {
            gioHangRepository.delete(gh);
            return "success: Đã xóa.";
        }

        Optional<SanPhamChiTiet> optSpct = sanPhamCTRepository.findById(gh.getIdSanPhamChiTiet());
        if (optSpct.isPresent() && optSpct.get().getSoLuong() < newQuantity) {
            return "error: Vượt quá tồn kho.";
        }

        gh.setSoLuong(newQuantity);
        gh.setNgaySua(LocalDateTime.now());
        gioHangRepository.save(gh);
        return "success";
    }

    @DeleteMapping("/cart/remove/{id}")
    @ResponseBody
    public String removeCartItem(@PathVariable("id") Integer cartItemId) {
        Optional<GioHang> gh = gioHangRepository.findById(cartItemId);
        if (gh.isPresent() && gh.get().getIdKhachHang().equals(getCurrentCustomerId())) {
            gioHangRepository.deleteById(cartItemId);
            return "success";
        }
        return "error";
    }

    @GetMapping("/api/cart")
    @ResponseBody
    public List<GioHangDTO> getCartJson() {
        Integer idKhachHang = getCurrentCustomerId();
        if (idKhachHang == null) return new ArrayList<>();
        return mapToDto(gioHangRepository.findByIdKhachHang(idKhachHang));
    }

    @PostMapping("/cart/clear-after-checkout")
    @ResponseBody
    public String clearCartAfterCheckout(@RequestBody List<Integer> cartItemIds) {
        try {
            if (cartItemIds != null && !cartItemIds.isEmpty()) {
                // Chỉ xóa nếu các ID này thuộc về khách hàng hiện tại (có thể bổ sung check ở đây)
                gioHangRepository.deleteAllById(cartItemIds);
                return "success";
            }
            return "error";
        } catch (Exception e) {
            return "error";
        }
    }
}