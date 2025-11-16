package sd_04.datn_fstore.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data // (Cần Lombok) Hoặc tự tạo Getter/Setter
public class CartItemDto {
    // Tên biến phải khớp với JSON từ Javascript
    private Integer sanPhamChiTietId;
    private Integer soLuong;
    private Double donGia; // JS gửi lên là 120000.0 (double)
}