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

    // --- [SỬA LỖI ĐỎ] CONSTRUCTOR CHỈ DÙNG CHO @Query (Chỉ có Tên và Tổng số lượng) ---
    // Constructor này khớp với: SELECT NEW TopProductDTO(Tên, SUM(Số lượng))
    public TopProductDTO(String productName, Long totalSold) {
        this.productName = productName;
        this.totalSold = totalSold;
    }

    // Constructor cũ của bạn (Nếu bạn dùng nó cho mục đích khác, hãy giữ lại)
    public TopProductDTO(String productName, BigDecimal price, Long totalSold) {
        this.productName = productName;
        this.price = price;
        this.totalSold = totalSold;
    }
}