package sd_04.datn_fstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor // Constructor cho tất cả 8 trường
public class CalculateTotalResponse {

    // 6 trường cơ bản cho kết quả tính toán
    private BigDecimal subTotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;
    private String voucherMessage;
    private boolean voucherValid;

    // 2 trường bổ sung mới (có thể dùng để báo lỗi hoặc thông tin chi tiết)
    private boolean outOfStock;
    private Integer SanPhamChiTietId;

    /**
     * Constructor 6 tham số để tương thích với cách gọi trong CheckoutServiceImpl (logic cũ)
     * Constructor này sẽ khởi tạo outOfStock = false và SanPhamChiTietId = null
     */
    public CalculateTotalResponse(BigDecimal subTotal, BigDecimal shippingFee, BigDecimal discountAmount,
                                  BigDecimal finalTotal, String voucherMessage, boolean voucherValid) {
        this.subTotal = subTotal;
        this.shippingFee = shippingFee;
        this.discountAmount = discountAmount;
        this.finalTotal = finalTotal;
        this.voucherMessage = voucherMessage;
        this.voucherValid = voucherValid;

        // Khởi tạo giá trị mặc định cho các trường mới
        this.outOfStock = false;
        this.SanPhamChiTietId = null;
    }
}