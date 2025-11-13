package sd_04.datn_fstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data // (Cần Lombok) Hoặc tự tạo Getter/Setter
public class CreateOrderRequest {
    // Tên biến phải khớp với JSON từ Javascript
    private Integer nhanVienId;
    private Integer khachHangId;
    private List<CartItemDto> itemsList;
    private Double totalAmount;
    private Double discountAmount;
    private String paymentMethod; // (CASH, TRANSFER, QR)
}