package sd_04.datn_fstore.controller.author;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import sd_04.datn_fstore.dto.GioHangDTO;
import sd_04.datn_fstore.model.GioHang;
import sd_04.datn_fstore.model.SanPhamChiTiet;
import sd_04.datn_fstore.repository.GioHangRepository;
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

    // Gán cứng ID khách hàng để TEST
    private Integer getCurrentCustomerId(HttpSession session) {
        return 1;
    }

    /**
     * Ánh xạ từ GioHang Entity sang GioHangDTO (Phiên bản An toàn)
     */
    private List<GioHangDTO> mapToDto(List<GioHang> gioHangs) {
        List<GioHangDTO> dtoList = new ArrayList<>();

        for (GioHang gh : gioHangs) {

            if (gh.getIdSanPhamChiTiet() == null) {
                System.err.println("LOG_ERROR: GioHang ID " + gh.getId() + " thiếu idSanPhamChiTiet.");
                continue;
            }

            Optional<SanPhamChiTiet> optSpct = sanPhamCTRepository.findById(gh.getIdSanPhamChiTiet());

            if (optSpct.isPresent()) {
                SanPhamChiTiet spct = optSpct.get();

                if (spct.getMauSac() == null || spct.getKichThuoc() == null || spct.getSanPham() == null) {
                    System.err.println("LOG_ERROR: SPCT ID " + spct.getId() + " thiếu MauSac/KichThuoc/SanPham (Kiểm tra dữ liệu DB).");
                    continue;
                }

                // --- Lấy dữ liệu an toàn ---
                BigDecimal donGia = spct.getGiaTien();
                String tenMau = spct.getMauSac().getTenMauSac();
                String tenKichCo = spct.getKichThuoc().getTenKichThuoc();
                Integer soLuongTon = spct.getSoLuong();

                String tenHinhAnh = "no-image.png";
                if (spct.getSanPham().getHinhAnh() != null && !spct.getSanPham().getHinhAnh().isEmpty()) {
                    tenHinhAnh = spct.getSanPham().getHinhAnh().get(0).getTenHinhAnh();
                }

                // Xây dựng DTO
                GioHangDTO dto = GioHangDTO.builder()
                        .id(gh.getId())
                        .soLuong(gh.getSoLuong() != null ? gh.getSoLuong() : 1)
                        .idSanPhamChiTiet(spct.getId())
                        .donGia(donGia)
                        .idSanPham(spct.getSanPham().getId())
                        .tenSanPham(spct.getSanPham().getTenSanPham())
                        .tenMau(tenMau)
                        .tenKichCo(tenKichCo)
                        .tenHinhAnh(tenHinhAnh)
                        .soLuongTon(soLuongTon)
                        .build();

                dtoList.add(dto);
            } else {
                System.err.println("LOG_ERROR: Không tìm thấy SanPhamChiTiet cho ID: " + gh.getIdSanPhamChiTiet());
            }
        }
        return dtoList;
    }


    // --- 1. HIỂN THỊ GIỎ HÀNG (GET /cart) ---
    @GetMapping("cart")
    public String index(Model model, HttpSession session) {
        Integer idKhachHang = getCurrentCustomerId(session);

        if (idKhachHang == null) {
            return "redirect:/login";
        }

        List<GioHang> gioHangs = gioHangRepository.findByIdKhachHang(idKhachHang);
        List<GioHangDTO> cartItems = mapToDto(gioHangs);

        // Tính toán tổng
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

    // --- 2. THÊM SẢN PHẨM VÀO GIỎ HÀNG (POST /cart/add) ---
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
                            @RequestParam("soLuong") Integer soLuong,
                            HttpSession session,
                            RedirectAttributes ra) {

        Integer idKhachHang = getCurrentCustomerId(session);
        if (idKhachHang == null) {
            System.err.println("LOG_ERROR: Khách hàng chưa đăng nhập.");
            return "redirect:/login";
        }

        Optional<SanPhamChiTiet> optSpct = sanPhamCTRepository.findById(idSanPhamChiTiet);
        if (optSpct.isEmpty()) {
            System.err.println("LOG_ERROR: Không tìm thấy SPCT ID: " + idSanPhamChiTiet);
            ra.addFlashAttribute("error", "Sản phẩm không tồn tại.");
            return "redirect:/";
        }
        SanPhamChiTiet spct = optSpct.get();

        // Kiểm tra tồn kho
        if (spct.getSoLuong() == null || spct.getSoLuong() < soLuong) {
            System.err.println("LOG_ERROR: Vượt tồn kho ban đầu. SPCT ID: " + idSanPhamChiTiet + ", Tồn: " + spct.getSoLuong());
            ra.addFlashAttribute("error", "Số lượng yêu cầu vượt quá tồn kho hiện tại.");
            return "redirect:/sanpham/chi/tiet/" + idSanPhamChiTiet;
        }

        Optional<GioHang> existingCartItem = gioHangRepository.findByIdKhachHangAndIdSanPhamChiTiet(
                idKhachHang, idSanPhamChiTiet
        );

        try {
            if (existingCartItem.isPresent()) {
                // --- CẬP NHẬT SỐ LƯỢNG ---
                GioHang gh = existingCartItem.get();
                int newQuantity = gh.getSoLuong() + soLuong;

                if (spct.getSoLuong() < newQuantity) {
                    ra.addFlashAttribute("error", "Tổng số lượng vượt quá tồn kho.");
                    return "redirect:/sanpham/chi/tiet/" + idSanPhamChiTiet;
                }

                gh.setSoLuong(newQuantity);
                gh.setNgaySua(LocalDateTime.now());
                gioHangRepository.save(gh);
                System.out.println("LOG_SUCCESS: Đã cập nhật GioHang ID: " + gh.getId());

            } else {
                // --- THÊM MỚI ---
                GioHang newCartItem = new GioHang();

                newCartItem.setIdKhachHang(idKhachHang);
                newCartItem.setIdSanPhamChiTiet(idSanPhamChiTiet);

                // ⭐️ FIX LỖI: Sử dụng ID thô trực tiếp (spct.getIdSanPham())
                if (spct.getIdSanPham() == null) {
                    System.err.println("LOG_FATAL: SPCT ID " + idSanPhamChiTiet + " thiếu idSanPham trong DB.");
                    ra.addFlashAttribute("error", "Lỗi dữ liệu sản phẩm.");
                    return "redirect:/sanpham/chi/tiet/" + idSanPhamChiTiet;
                }

                // Gán ID thô vào cột Foreign Key
                newCartItem.setIdSanPham(spct.getIdSanPham());

                newCartItem.setSoLuong(soLuong);
                newCartItem.setNgayTao(LocalDateTime.now());
                newCartItem.setNgaySua(LocalDateTime.now());
                newCartItem.setTrangThai(1);

                GioHang savedItem = gioHangRepository.save(newCartItem);
                System.out.println("LOG_SUCCESS: Đã THÊM MỚI GioHang ID: " + savedItem.getId());
            }

        } catch (Exception e) {
            e.printStackTrace(System.err);
            ra.addFlashAttribute("error", "Lỗi hệ thống khi lưu giỏ hàng: " + e.getMessage());
            return "redirect:/sanpham/chi/tiet/" + idSanPhamChiTiet;
        }

        // Chuyển hướng về trang Giỏ hàng
        ra.addFlashAttribute("success", "Sản phẩm đã được thêm vào giỏ hàng thành công!");
        return "redirect:/cart";
    }

    // --- 3. CẬP NHẬT SỐ LƯỢNG (POST /cart/update) ---
    // (Chức năng AJAX trên trang cart.html)
    @PostMapping("/cart/update")
    @ResponseBody
    public String updateCartItem(@RequestParam("id") Integer cartItemId,
                                 @RequestParam("quantityChange") Integer change) {

        Optional<GioHang> optGh = gioHangRepository.findById(cartItemId);
        if (optGh.isEmpty()) {
            return "error: Mục giỏ hàng không tồn tại.";
        }

        GioHang gh = optGh.get();
        int newQuantity = gh.getSoLuong() + change;

        if (newQuantity < 1) {
            gioHangRepository.delete(gh);
            return "success: Đã xóa sản phẩm khỏi giỏ hàng.";
        }

        Optional<SanPhamChiTiet> optSpct = sanPhamCTRepository.findById(gh.getIdSanPhamChiTiet());
        if (optSpct.isPresent() && optSpct.get().getSoLuong() < newQuantity) {
            return "error: Số lượng yêu cầu vượt quá tồn kho.";
        }

        try {
            gh.setSoLuong(newQuantity);
            gh.setNgaySua(LocalDateTime.now());
            gioHangRepository.save(gh);
            return "success: Cập nhật số lượng thành công.";
        } catch (Exception e) {
            System.err.println("LOG_ERROR: Lỗi cập nhật giỏ hàng AJAX: " + e.getMessage());
            return "error: Lỗi DB khi cập nhật.";
        }
    }

    // --- 4. XÓA SẢN PHẨM (DELETE /cart/remove/{id}) ---
    // (Chức năng AJAX trên trang cart.html)
    @DeleteMapping("/cart/remove/{id}")
    @ResponseBody
    public String removeCartItem(@PathVariable("id") Integer cartItemId) {
        try {
            gioHangRepository.deleteById(cartItemId);
            return "success: Xóa sản phẩm khỏi giỏ hàng thành công.";
        } catch (Exception e) {
            System.err.println("LOG_ERROR: Lỗi xóa giỏ hàng AJAX: " + e.getMessage());
            return "error: Không thể xóa sản phẩm.";
        }
    }
    // ==== API JSON CHO CHECKOUT ====
    @GetMapping("/api/cart")
    @ResponseBody
    public List<GioHangDTO> getCartJson(HttpSession session) {
        Integer idKhachHang = getCurrentCustomerId(session);

        if (idKhachHang == null) {
            return new ArrayList<>();
        }

        List<GioHang> gioHangs = gioHangRepository.findByIdKhachHang(idKhachHang);
        return mapToDto(gioHangs);
    }

    /// //API HIEN THI CHECK OUT(THANH TOAN)
//    @GetMapping("/checkout")
//    public String checkout(Model model, HttpSession session) {
//        Integer idKhachHang = getCurrentCustomerId(session);
//
//        if (idKhachHang == null) {
//            return "redirect:/login";
//        }
//
//        // Lấy giỏ hàng theo khách hàng
//        List<GioHang> gioHangs = gioHangRepository.findByIdKhachHang(idKhachHang);
//        List<GioHangDTO> cartItems = mapToDto(gioHangs);
//
//        if (cartItems.isEmpty()) {
//            // Nếu giỏ rỗng -> quay về giỏ
//            model.addAttribute("error", "Giỏ hàng của bạn đang trống!");
//            return "redirect:/cart";
//        }
//
//        // Tính tổng
//        BigDecimal totalPrice = BigDecimal.ZERO;
//        for (GioHangDTO item : cartItems) {
//            totalPrice = totalPrice.add(item.getThanhTien());
//        }
//
//        model.addAttribute("cartItems", cartItems);
//        model.addAttribute("totalPrice", totalPrice);
//
//        // Gọi tới checkout.html
//        return "view/author/checkout";
//    }

}