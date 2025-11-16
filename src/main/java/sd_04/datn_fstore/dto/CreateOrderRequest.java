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
    private String orderCode;       // Mã hóa đơn (HD...)
    private String paymentMethod;   // (CASH, VNPAY, DRAFT...)

    // 2. Thông tin tiền (tạm tính từ client)
    private BigDecimal totalAmount;     // Tổng tiền cuối (để server tham khảo)
    private BigDecimal discountAmount;  // Tiền giảm (để server tham khảo)

    // 3. Danh sách sản phẩm trong giỏ hàng
    private List<Item> itemsList;

    /**
     * Lớp con đại diện cho 1 sản phẩm trong giỏ hàng
     */
    @Data // Tự động tạo getter/setter
    public static class Item {
        private Integer sanPhamChiTietId;
        private Integer soLuong;
        private BigDecimal donGia; // Giá bán tại thời điểm thêm vào giỏ

        // Constructor để code VNPAY (trong VnPayServiceImpl) dễ dùng
        public Item(Integer sanPhamChiTietId, Integer soLuong, BigDecimal donGia) {
            this.sanPhamChiTietId = sanPhamChiTietId;
            this.soLuong = soLuong;
            this.donGia = donGia;
        }
        private BigDecimal customerPaidAmount;
    }
}