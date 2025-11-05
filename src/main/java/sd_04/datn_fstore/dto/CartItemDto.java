package sd_04.datn_fstore.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDto {
    private Integer productId; // <-- THAY ĐỔI TỪ String SANG Integer
    private int quantity;
    private BigDecimal price;
}