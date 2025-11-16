package sd_04.datn_fstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO (Data Transfer Object) này dùng để
 * trả về URL thanh toán VNPAY (hoặc thông báo lỗi)
 * cho JavaScript (frontend).
 */
@Data // Tự động tạo getter, setter, toString, equals, hashCode
@NoArgsConstructor // Tự động tạo constructor rỗng
@AllArgsConstructor // Tự động tạo constructor với tất cả các tham số
public class VnPayResponseDTO {

    /**
     * Trạng thái (true = thành công, false = thất bại)
     * JS sẽ đọc: data.success
     */
    private boolean success;

    /**
     * Thông báo (VD: "Tạo link VNPAY thành công" hoặc "Lỗi...")
     * JS sẽ đọc: data.message
     */
    private String message;

    /**
     * Đường dẫn URL để chuyển hướng khách hàng sang VNPAY
     * JS sẽ đọc: data.paymentUrl
     */
    private String paymentUrl;

}