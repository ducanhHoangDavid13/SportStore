package sd_04.datn_fstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckoutResponse {

    /**
     * Trạng thái xử lý:
     * - true: Thành công (đã tạo đơn hoặc tạo link thanh toán)
     * - false: Thất bại (lỗi validate, hết hàng, lỗi hệ thống...)
     */
    private boolean success;

    /**
     * Thông báo để hiển thị cho người dùng (alert hoặc toast message)
     * Ví dụ: "Đặt hàng thành công", "Sản phẩm đã hết hàng", "Lỗi tạo link VNPAY"...
     */
    private String message;

    /**
     * Đường dẫn chuyển hướng (nếu thành công)
     * - Nếu là COD: Chuyển về trang "/checkout/success?id=..."
     * - Nếu là VNPAY: Chuyển sang URL thanh toán của VNPAY (https://sandbox.vnpayment.vn/...)
     * - Nếu thất bại: Có thể để null
     */
    private String redirectUrl;
}