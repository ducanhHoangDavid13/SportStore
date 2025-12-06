package sd_04.datn_fstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// Nếu dùng Spring Boot 2.x thì dùng javax.validation...
// Nếu dùng Spring Boot 3.x thì dùng jakarta.validation...
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutRequest {

    // --- 1. ĐỊNH DANH ---
    private Integer khachHangId; // (QUAN TRỌNG) Để biết khách hàng nào đang mua

    // --- 2. THÔNG TIN NGƯỜI NHẬN ---
    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    // --- 3. ĐỊA CHỈ GIAO HÀNG ---
    @NotBlank(message = "Tỉnh/Thành phố không được để trống")
    private String city;

    @NotBlank(message = "Quận/Huyện không được để trống")
    private String district;

    @NotBlank(message = "Phường/Xã không được để trống")
    private String ward;

    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    private String addressDetail;

    // --- 4. THÔNG TIN ĐƠN HÀNG ---
    private String note; // Ghi chú đơn hàng

    @NotBlank(message = "Vui lòng chọn phương thức thanh toán")
    private String paymentMethod; // Giá trị: "COD", "VNPAY", "TRANSFER"

    // Voucher
    private String voucherCode;
    private Integer phieuGiamGiaId; // Thêm trường này để tiện xử lý nếu Frontend gửi ID

    // --- 5. THÔNG TIN TÀI CHÍNH (Có thể Frontend gửi hoặc Backend tự tính) ---
    private BigDecimal shippingFee = BigDecimal.ZERO;
    private BigDecimal tongTien;        // Tổng tiền hàng
    private BigDecimal tienGiamGia;     // Số tiền được giảm
    private BigDecimal tongTienSauGiam; // Số tiền khách phải trả (Final)

    // --- 6. GIỎ HÀNG ---
    @NotEmpty(message = "Giỏ hàng không được để trống")
    private List<CartItem> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CartItem {
        @NotNull(message = "ID sản phẩm chi tiết không được để trống")
        private Integer sanPhamChiTietId;

        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        private Integer soLuong;

        private BigDecimal donGia;
    }

    // Helper: Lấy địa chỉ đầy đủ
    public String getFullAddress() {
        return (addressDetail == null ? "" : addressDetail) + ", " +
                (ward == null ? "" : ward) + ", " +
                (district == null ? "" : district) + ", " +
                (city == null ? "" : city);
    }
}