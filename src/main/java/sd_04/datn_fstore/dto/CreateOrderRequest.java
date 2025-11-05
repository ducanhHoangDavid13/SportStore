package sd_04.datn_fstore.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateOrderRequest {
    private Integer customerId; // <-- THAY ĐỔI TỪ String SANG Integer
    private List<CartItemDto> items;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private String paymentMethod;
}