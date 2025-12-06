package sd_04.datn_fstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDTO {
    private String productName;
    private Long totalSold;
    private BigDecimal price;
    private String productImage;

    // --- [QUAN TRỌNG] THÊM CONSTRUCTOR NÀY ĐỂ FIX LỖI ĐỎ ---
    // Thứ tự tham số phải khớp 100% với câu @Query
    // 1. String (Tên)
    // 2. BigDecimal (Giá - MAX(donGia))
    // 3. Long (Số lượng - SUM(soLuong))
    public TopProductDTO(String productName, BigDecimal price, Long totalSold) {
        this.productName = productName;
        this.price = price;
        this.totalSold = totalSold;
    }
}