package sd_04.datn_fstore.dto;

import lombok.Data;

@Data
public class PaymentNotificationDto {
    // Tên các trường này phải khớp 100% với JSON mà dịch vụ webhook gửi
    private boolean success;
    private String description; // Nội dung (mã hóa đơn)
    private long amount; // Số tiền (thường là long hoặc int)
    // Thêm các trường khác nếu cần
}