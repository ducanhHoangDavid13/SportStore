package sd_04.datn_fstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * Lớp này dùng để hứng dữ liệu
 * khi JavaScript gọi API thanh toán hoặc lưu tạm.
 */
@Data // Tự động tạo getter/setter
public class CreateOrderRequest {

    // 1. Thông tin đơn hàng
    private Integer nhanVienId;
    private Integer khachHangId;    // Có thể là null (khách lẻ)
    private Integer phieuGiamGiaId; // Có thể là null (không dùng)

    private String maHoaDon;            // (Trước là orderCode)
    private String phuongThucThanhToan; // (Trước là paymentMethod: COD, VNPAY...)

    // 2. Thông tin tiền (tạm tính từ client)
    private BigDecimal tongTien;    // (Trước là totalAmount) - Tổng tiền cuối cùng
    private BigDecimal tienGiamGia; // (Trước là discountAmount)

    // 3. Danh sách sản phẩm trong giỏ hàng
    private List<SanPhamItem> danhSachSanPham; // (Trước là itemsList)

    /**
     * Lớp con đại diện cho 1 sản phẩm trong giỏ hàng
     */
    @Data // Tự động tạo getter/setter
    public static class SanPhamItem { // (Trước là Item)
        private Integer sanPhamChiTietId;
        private Integer soLuong;
        private BigDecimal donGia;

        // Constructor để code VNPAY dễ dùng
        public SanPhamItem(Integer sanPhamChiTietId, Integer soLuong, BigDecimal donGia) {
            this.sanPhamChiTietId = sanPhamChiTietId;
            this.soLuong = soLuong;
            this.donGia = donGia;
        }

        private BigDecimal tienKhachDua; // (Trước là customerPaidAmount - Dùng cho bán tại quầy)
    }
}