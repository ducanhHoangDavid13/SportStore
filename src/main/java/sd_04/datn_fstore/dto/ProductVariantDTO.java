package sd_04.datn_fstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantDTO {
    private Integer id;          // ID của SanPhamChiTiet (Cái cần gửi đi Checkout)
    private String tenMau;       // Tên màu
    private String tenSize;      // Tên size
    private BigDecimal giaTien;  // Giá tiền riêng của biến thể (nếu có)
    private Integer soLuongTon;  // Số lượng trong kho
    private String anh;          // Tên ảnh của biến thể (nếu có)
}